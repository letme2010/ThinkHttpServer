
package org.cxt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.cxt.NanoHTTPD.Response.Status;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;


public class LtHTTPD extends NanoHTTPD {

    public static final String INDEX_HTML = "android_view_firebug_index.html";

    public interface IDelegate {

        String getResourceTextByName(String aResName);

        String getNotFoundResPageText();

        Bitmap getTextImage();

        String getViewHieracryJSON();

		Bitmap getBitmapByViewHashCode(int aHashCode);

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

        if (uri.endsWith("png")) {
            // request image.
        	
        	//url like : http://hostname/viewSnap/12312312.png

//            Bitmap bitmap = this.mDelegate.getTextImage();

        	int indexOfHashCodeStart = uri.indexOf("viewSnap") + "viewSnap".length() + 1;
        	int indexOfPngSuffix = uri.indexOf("png") - 1;
        	String hashCode = uri.substring(indexOfHashCodeStart, indexOfPngSuffix);
         	
        	Bitmap bitmap = this.mDelegate.getBitmapByViewHashCode(Integer.valueOf(hashCode));
        	
        	if (null == bitmap) {
        		return null;
        	}
        	
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.PNG, 100 /* ignored for PNG */, bos);
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
