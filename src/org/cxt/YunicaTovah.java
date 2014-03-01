package org.cxt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cxt.LtHTTPD.IDelegate;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

public class YunicaTovah extends Activity implements IDelegate {

	private static IHTTPD mHttpd;

	private IHTTPD getHttpd() {
		if (null == this.mHttpd) {
			this.mHttpd = new LtHTTPD(this);
		}
		return this.mHttpd;
	}

	private void initHttpd() {
		if (null == this.mHttpd) {
			try {
				this.getHttpd().start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Activity hook.
	 */

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.initHttpd();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	protected void onPause() {
		super.onPause();
	};

	@Override
	protected void onStop() {
		super.onStop();
	}

	protected void onDestroy() {
		super.onDestroy();
	};

	/**
	 * IHttpd Delegate functions.
	 */

	@Override
	public String getResourceTextByName(String aResName) {
		try {
			StringBuilder buf = new StringBuilder();
			InputStream resText;
			resText = getAssets().open(aResName);
			BufferedReader in = new BufferedReader(new InputStreamReader(resText));
			String str;

			while ((str = in.readLine()) != null) {
				buf.append(str);
			}

			in.close();

			return buf.toString();
		} catch (IOException e) {

			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getNotFoundResPageText() {
		try {
			StringBuilder buf = new StringBuilder();
			InputStream resText;
			resText = getAssets().open("404.html");
			BufferedReader in = new BufferedReader(new InputStreamReader(resText));
			String str;

			while ((str = in.readLine()) != null) {
				buf.append(str);
			}

			in.close();

			return buf.toString();
		} catch (IOException e) {

			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Bitmap getTextImage() {
		return BitmapFactory.decodeResource(this.getResources(), R.drawable.gundam);
	}

	private class Node {
		private List<Node> mChildren = new ArrayList<Node>();

		private Map<String, Object> mAttrMap = new HashMap<String, Object>();

		public List<Node> getChildren() {
			return mChildren;
		}

		public Map<String, Object> getAttrMap() {
			return mAttrMap;
		}

		public String toJSONString() {
			try {

				JSONObject retJSON = new JSONObject();

				Set<String> attrKeys = mAttrMap.keySet();
				for (String attrKey : attrKeys) {
					retJSON.put(attrKey, mAttrMap.get(attrKey));
				}

				JSONArray childArray = new JSONArray();
				for (Node node : this.mChildren) {
					childArray.put(new JSONObject(node.toJSONString()));
				}
				retJSON.put("children", childArray);

				return retJSON.toString();

			} catch (Exception e) {
				return null;
			}
		}

	}

	private Node view2Node(View aView) {
		if (null == aView) {
			return null;
		}

		Node ret = new Node();
		ret.getAttrMap().put("class", aView.getClass().getSimpleName());
		ret.getAttrMap().put("hashCode", aView.hashCode());

		JSONArray ltrb = new JSONArray();
		ltrb.put(aView.getLeft());
		ltrb.put(aView.getTop());
		ltrb.put(aView.getRight());
		ltrb.put(aView.getBottom());
		ret.getAttrMap().put("ltrb", ltrb);

		if (aView instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) aView;
			List<Node> children = ret.getChildren();
			for (int i = 0, len = viewGroup.getChildCount(); i < len; i++) {
				children.add(view2Node(viewGroup.getChildAt(i)));
			}
		}

		return ret;
	}

	@Override
	public String getViewHieracryJSON() {
		Window window = this.getWindow();

		View decorView = window.getDecorView();
		return this.view2Node(decorView).toJSONString();
	}

	@Override
	public Bitmap getBitmapByViewHashCode(int aHashCode) {

		View tagView = findViewByHashCode(this.getWindow().getDecorView(), aHashCode);
		if (null != tagView) {
			return loadBitmapFromView2(tagView);
		}

		return null;

	}

	private static View findViewByHashCode(View aView, int aHashCode) {
		View ret = null;

		do {

			if ((null == aView) || (0 >= aHashCode)) {
				break;
			}

			if (aHashCode == aView.hashCode()) {
				ret = aView;
				break;
			}

			if (aView instanceof ViewGroup) {

				ViewGroup viewGroup = (ViewGroup) aView;

				for (int i = 0, len = viewGroup.getChildCount(); i < len; i++) {
					View childView = viewGroup.getChildAt(i);
					ret = findViewByHashCode(childView, aHashCode);
					if (null != ret) {
						break;
					}
				}

			}

		} while (false);

		return ret;
	}

	/**
	 * View的截图,包含childView.
	 * 
	 * @param v
	 * @return
	 */
	public Bitmap shotSnap(View v) {
		try {
			Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(b);
			v.draw(c);
			return b;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public Bitmap loadBitmapFromView2(View v) {
		return createSnapshot(v, Bitmap.Config.ARGB_8888, Color.TRANSPARENT, false);
	}

	private static Bitmap createSnapshot(View aView, Bitmap.Config quality, int backgroundColor, boolean skipChildren) {

		Method createSnapShotMethod = null;

		// try {

		for (Method method : View.class.getDeclaredMethods()) {
			System.out.println("LETME:" + method.getName());
			if ("createSnapshot".equals(method.getName())) {
				createSnapShotMethod = method;
				System.out.println("LETME: FIND IT.");
				break;
			}
		}

		if (null != createSnapShotMethod) {
			createSnapShotMethod.setAccessible(true);
		}

		if (null != createSnapShotMethod) {
			try {
				return (Bitmap) createSnapShotMethod.invoke(aView, quality, backgroundColor, skipChildren);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (null != createSnapShotMethod) {
			createSnapShotMethod.setAccessible(false);
		}

		return null;
	}

	/**
	 * 加载View独立的截图,不包含childView.
	 * 
	 * @param v
	 * @return
	 * @throws Exception
	 */
	public Bitmap loadBitmapFromView(View v) {

		try {

			if ((v instanceof View) && !(v instanceof ViewGroup)) {
				return shotSnap(v);
			} else {

				Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
				Canvas c = new Canvas(b);
				// draw background
				Drawable backgroundDrawable = v.getBackground();
				if (null != backgroundDrawable) {
					backgroundDrawable.draw(c);
				}

				// draw content
				Class<? extends View> viewClass = v.getClass();

				Constructor<?>[] constructorArray = viewClass.getDeclaredConstructors();
				Constructor<?> defaultConstructor = null;

				for (Constructor<?> constructor : constructorArray) {
					Class<?>[] parameterTypes = constructor.getParameterTypes();
					if (1 == parameterTypes.length && parameterTypes[0].equals(Context.class)) {
						defaultConstructor = constructor;
						break;
					}
				}

				if (null == defaultConstructor) {

				} else {

					Object newInstance = defaultConstructor.newInstance(this);

					for (Field field : viewClass.getDeclaredFields()) {
						boolean originAccessible = field.isAccessible();
						field.setAccessible(true);
						// System.out.println("LETME Field[" + field.getName() +
						// ","
						// +
						// field.get(v) + "]");
						field.set(newInstance, field.get(v));
						// System.out.println("LETME Field[" + field.getName() +
						// ","
						// +
						// field.get(newInstance) + "]");
						field.setAccessible(originAccessible);
					}

					if (newInstance instanceof ViewGroup) {
						ViewGroup view = (ViewGroup) newInstance;
						view.removeAllViews();
					}

					if (newInstance instanceof View) {
						View view = (View) newInstance;

						view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
						view.invalidate();

						view.draw(c);
					}

				}
				return b;

			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
}
