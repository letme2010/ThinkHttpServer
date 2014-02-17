
package com.example.thinkhttpserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import com.example.thinkhttpserver.NanoHTTPD.Response.Status;

public class LtHTTPD extends NanoHTTPD {

    public static final String INDEX_HTML = "android_view_firebug_index.html";

    public interface IDelegate {

        String getResourceTextByName(String aResName);

        String getNotFoundResPageText();

        Bitmap getTextImage();

        String getViewHieracryJSON();

    }

    private IDelegate mDelegate;

    public LtHTTPD(IDelegate aDelegate) {
        super(9527);
        this.mDelegate = aDelegate;
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();

        System.out.println("LTHTTPD:" + method + " '" + uri + "' ");

        if (uri.lastIndexOf(".png") > 0) {
            // request image.

            Bitmap bitmap = this.mDelegate.getTextImage();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 0 /* ignored for PNG */, bos);
            byte[] bitmapdata = bos.toByteArray();
            ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

            NanoHTTPD.Response imageRsp = new NanoHTTPD.Response(Status.OK, "image/png", bs);
            return imageRsp;
        } else {
            String vhJSON = this.mDelegate.getViewHieracryJSON();
            return new NanoHTTPD.Response(vhJSON);
        }

    }

    private String getNotFoundResPageText() {
        return this.mDelegate.getNotFoundResPageText();
    }

    private String getResourceTextByName(String aResName) {
        String ret = null;

        ret = this.mDelegate.getResourceTextByName(aResName);

        return ret;
    }

}
