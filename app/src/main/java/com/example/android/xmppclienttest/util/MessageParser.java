package com.example.android.xmppclienttest.util;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MessageParser {

    private static final String ns = "pubsub:test:test";

    public List parse(String rawXml) throws XmlPullParserException, IOException {
        InputStream stream = new ByteArrayInputStream(rawXml.getBytes());

        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(stream, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            stream.close();
        }
    }

    private List readFeed(XmlPullParser parser) throws IOException, XmlPullParserException {
        List entries = new ArrayList();

        parser.require(XmlPullParser.START_TAG, ns, "feed");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("message")) {
                entries.add(readEntry(parser));
            } else {
                skip(parser);
            }
        }

        return entries;
    }

    private Object readEntry(XmlPullParser parser) throws IOException, XmlPullParserException {
        String body = null;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("body")) {
                body = readBody(parser);
            }
        }
        return null;
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

    private void skip(XmlPullParser parser) {

    }

}
