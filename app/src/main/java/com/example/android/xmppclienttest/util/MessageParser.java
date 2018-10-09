package com.example.android.xmppclienttest.util;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MessageParser {

    private static final String ns = "pubsub:test:test";

    public String retreiveXmlString(String message) {
        int firstTagPosition = message.indexOf('<');
        int lastTagPosition = message.lastIndexOf('>');

        return message.substring(firstTagPosition, lastTagPosition + 1);
    }

    public String parseTest(String rawXml) throws XmlPullParserException, IOException {
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

    private String readMessage(XmlPullParser parser) throws IOException, XmlPullParserException {
        String message = null;
        parser.require(XmlPullParser.START_TAG, ns, "message");
        parser.next();
        message = readBody(parser);
        parser.next();
        parser.require(XmlPullParser.END_TAG, ns, "message");
        return message;
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
