package net.senmori.btsuite.util;

import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;


public class PasteUtil {

    public static String post(String data) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost( "https://hastebin.com/documents" );

        try {
            post.setEntity( new StringEntity( data ) );

            HttpResponse response = client.execute( post );
            String result = EntityUtils.toString( response.getEntity() );
            return "https://hastebin.com/" + new JsonParser().parse( result ).getAsJsonObject().get( "key" ).getAsString();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return "Could not post!";
    }

    // I added this method :)
    public static StringSelection copyStringToClipboard(String selection) {
        StringSelection sel = new StringSelection( selection );
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents( sel, sel );
        return sel;
    }
}
