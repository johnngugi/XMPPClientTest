package com.example.android.xmppclienttest.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Xml;

import com.example.android.xmppclienttest.ApplicationContextProvider;
import com.example.android.xmppclienttest.database.MessageEntry;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MessageParser {

    private static final String ns = "jabber:client";

    public String retreiveXmlString(String message) {
        int firstTagPosition = message.indexOf('<');
        int lastTagPosition = message.lastIndexOf('>');

        return message.substring(firstTagPosition, lastTagPosition + 1);
    }

    private Bitmap convertBase64ToBitmap(String base64String) {
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    // TODO (3) Change name from parseTest parseContent when finished
    public MessageEntry parseTest(String rawXml) throws XmlPullParserException, IOException {
        InputStream stream = new ByteArrayInputStream(rawXml.getBytes());

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
            parser.setInput(stream, null);
            parser.nextTag();
            return readMessage(parser);
        } finally {
            stream.close();
        }
    }

    private MessageEntry readMessage(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "message");
        String subject = null;
        String body = null;
        String file = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            System.out.println("Tag name: " + name);
            switch (name) {
                case "subject":
                    subject = readSubject(parser);
                    break;
                case "body":
                    body = readBody(parser);
                    break;
                case "file":
                    file = readFile(parser);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        MessageEntry messageEntry = new MessageEntry(subject, body);
        messageEntry.setFilePath(file);
        return messageEntry;
    }

    private String readFile(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "file");
        String fileName = parser.getAttributeValue(null, "name");
        String fileSize = parser.getAttributeValue(null, "size");
        Bitmap bitmapImage = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            System.out.println("Tag name: " + name);
            switch (name) {
                case "base64Bin":
                    bitmapImage = readBitmapImage(parser);
                    saveImageToStorage(bitmapImage, fileName);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }
        return fileName;
    }

    private void saveImageToStorage(Bitmap bitmapImage, String fileName) {
        File file = new File(ApplicationContextProvider.getContext().getFilesDir(), fileName);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 85, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap readBitmapImage(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "base64Bin");
        String base64String = readText(parser);
        Bitmap bitmapImage = convertBase64ToBitmap(base64String);
        parser.require(XmlPullParser.END_TAG, ns, "base64Bin");
        return bitmapImage;
    }

    private String readSubject(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "subject");
        String msg = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "subject");
        return msg;
    }

    private String readBody(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "body");
        String msg = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "body");
        return msg;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

}
