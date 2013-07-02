package com.mac.android.goalmania;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ws.munday.slidingmenu.MarginAnimation;
import ws.munday.slidingmenu.R;
import ws.munday.slidingmenu.R.layout;
import ws.munday.slidingmenu.Utility;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.mac.android.goalmania.model.MenuItemModel;

public abstract class CustomFragment extends SherlockFragmentActivity {
	public static final int MENU_TYPE_SLIDING = 1;
	public static final int MENU_TYPE_SLIDEOVER = 2;
	public static final int MENU_TYPE_PARALLAX = 3;

	public static final String SL_IMAGE = "SL_MENU_IMAGE";
	public static final String SL_TITLE = "SL_MENU_TITLE";
	public static final String SL_DESCRIPTION = "SL_MENU_DESCRIPTION";

	protected Object currentModel;
	protected ActionBar ab;

	protected ListView slidingMenuList;
	private int smContentId;

	private boolean mIsLayoutShown = false;
	private int mMenuWidth;
	public static final String LOG_TAG = "SlidingMenuActivity";
	private int mMenuLayoutId;
	private int mContentLayoutId;
	private long mAnimationDuration = 400;
	private int mMaxMenuWidthDps = 375;
	private int mMinMainWidthDps = 50;
	private Interpolator mInterpolator = new DecelerateInterpolator(1.2f);
	private int mType = MENU_TYPE_SLIDING;
	private boolean mSlideTitleBar = true;

	public CustomFragment() {
		this(true);
	}

	public CustomFragment(boolean slideTitleBar) {
		mSlideTitleBar = slideTitleBar;
	}

	protected final void restartActivity() {
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
	}

	protected ActionBar setActionBar() {
		ActionBar mActionBar = getSupportActionBar();
		mActionBar.setHomeButtonEnabled(true);
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(true);
		return mActionBar;
	}

	protected void showKeyBoard(View v) {
		v.requestFocus();
		getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}

	protected float convertPixelToDip(int pixel) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel,
				getResources().getDisplayMetrics());
	}

	protected void setVisibilityMenuOption(Menu menu, int id, boolean visible) {
		MenuItem item = menu.findItem(id);
		item.setVisible(visible);
	}

	protected void addMenuOption(Menu menu, int id, int order, int idString,
			int idDrawable, boolean visible, int action) {
		menu.add(0, id, order, getString(idString)).setIcon(idDrawable)
				.setVisible(visible).setShowAsAction(action);
	}

	protected abstract void initInterface();

	protected abstract void getIntentData();

	protected abstract void putIntentData();

	protected abstract Object loadDataBase();

	protected abstract void processActivity();

	protected abstract void initTitle(ActionBar bar);

	public void setLayoutIds(int menuLayoutId, int contentLayoutId) {
		mMenuLayoutId = menuLayoutId;
		mContentLayoutId = contentLayoutId;
	}

	public void setSlidingMenuContentId(int slidingMenuContentId) {
		smContentId = slidingMenuContentId;
	}

	public void setAnimationDuration(long duration) {
		mAnimationDuration = duration;
	}

	public void setMaxMenuWidth(int width) {
		mMaxMenuWidthDps = width;
	}

	public void setMinContentWidth(int width) {
		mMinMainWidthDps = width;
	}

	public void setAnimationType(int type) {
		mType = type;
	}

	public Interpolator getInterpolator() {
		return mInterpolator;
	}

	public void setInterpolator(Interpolator i) {
		mInterpolator = i;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);

		if (mContentLayoutId != 0) {
			if (!mSlideTitleBar) {

				setContentView(R.layout.ws_munday_slideovermenu);

				ViewGroup menu = (ViewGroup) findViewById(R.id.ws_munday_slidingmenu_menu_frame);
				ViewGroup content = (ViewGroup) findViewById(R.id.ws_munday_slidingmenu_content_frame);

				LayoutInflater li = getLayoutInflater();

				content.addView(li.inflate(mContentLayoutId, null));
				menu.addView(li.inflate(mMenuLayoutId, null));

				menu.setVisibility(View.GONE);

			} else {

				setContentView(mContentLayoutId);
				Window window = getWindow();

				ViewGroup decor = (ViewGroup) window.getDecorView();
				ViewGroup allcontent = (ViewGroup) decor.getChildAt(0);
				decor.removeView(allcontent);

				LayoutInflater li = getLayoutInflater();

				RelativeLayout main = (RelativeLayout) li.inflate(
						layout.ws_munday_slideovermenu, null);

				ViewGroup menu = (ViewGroup) main
						.findViewById(R.id.ws_munday_slidingmenu_menu_frame);
				ViewGroup content = (ViewGroup) main
						.findViewById(R.id.ws_munday_slidingmenu_content_frame);

				int statusbarHeight = (int) Utility.getTopStatusBarHeight(
						getResources(), getWindowManager());

				ViewGroup mnu = (ViewGroup) li.inflate(mMenuLayoutId, null);
				mnu.setPadding(mnu.getPaddingLeft(), mnu.getPaddingTop()
						+ statusbarHeight, mnu.getPaddingRight(),
						mnu.getPaddingTop());
				content.addView(allcontent);
				content.setBackgroundDrawable(Utility.getThemeBackground(this));
				menu.addView(mnu);

				decor.addView(main);
				menu.setVisibility(View.GONE);

			}

			initMenu(false);
		}

		if (mMenuLayoutId != 0  && smContentId != 0) {
			initSlidingMenu();
		}

		currentModel = loadDataBase();

		initInterface();

		getIntentData();

		initTitle(ab);
	}

	
	
	private void initSlidingMenu() {
		// R�cup�ration de la listview cr��e dans le fichier main.xml
		slidingMenuList = (ListView) findViewById(com.mac.android.goalmania.R.id.listview_menu);

		List<MenuItemModel> menuItems = getMenuListItem();

		// Cr�ation de la ArrayList qui nous permettra de remplire la listView
		ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
		for (MenuItemModel menuItemModel : menuItems) {

			// Cr�ation d'une HashMap pour ins�rer les informations du premier
			// item
			// de notre listView
			HashMap<String, String> map = new HashMap<String, String>();
			// on ins�re un �l�ment titre que l'on r�cup�rera dans le textView
			// titre
			// cr�� dans le fichier affichageitem.xml
			map.put(SL_TITLE, menuItemModel.getTitle());
			// on ins�re un �l�ment description que l'on r�cup�rera dans le
			// textView
			// description cr�� dans le fichier affichageitem.xml
			map.put(SL_DESCRIPTION, menuItemModel.getDescription());
			// on ins�re la r�f�rence � l'image (convertit en String car
			// normalement
			// c'est un int) que l'on r�cup�rera dans l'imageView cr�� dans le
			// fichier affichageitem.xml
			map.put(SL_IMAGE, String.valueOf(menuItemModel.getImageId()));
			// enfin on ajoute cette hashMap dans la arrayList
			listItem.add(map);
		}

		// Cr�ation d'un SimpleAdapter qui se chargera de mettre les items
		// pr�sent dans notre list (listItem) dans la vue affichageitem
		SimpleAdapter mSchedule = new SimpleAdapter(
				this.getBaseContext(),
				listItem,smContentId,
				new String[] { SL_IMAGE, SL_TITLE, SL_DESCRIPTION },
				new int[] { com.mac.android.goalmania.R.id.sliding_menu_img,
						com.mac.android.goalmania.R.id.sliding_menu_titre,
						com.mac.android.goalmania.R.id.sliding_menu_description });

		// On attribut � notre listView l'adapter que l'on vient de cr�er
		slidingMenuList.setAdapter(mSchedule);

		// Enfin on met un �couteur d'�v�nement sur notre listView
		slidingMenuList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				// on r�cup�re la HashMap contenant les infos de notre item
				// (titre, description, img)
				HashMap<String, String> map = (HashMap<String, String>) slidingMenuList
						.getItemAtPosition(position);

				MenuItemModel itemModel = new MenuItemModel(Integer
						.parseInt(map.get(SL_IMAGE)), map.get(SL_TITLE), map
						.get(SL_DESCRIPTION));

				onSlidingMenuClick(a, v, position, id, itemModel);
			}
		});
	}

	
	protected void onSlidingMenuClick(AdapterView<?> a, View v, int position,
			long id, MenuItemModel itemModel) {
		System.out.println("otto");
	}

	protected List<MenuItemModel> getMenuListItem() {
		System.out.println("toto");
		MenuItemModel menuItem1 = new MenuItemModel(1, "Accueil", "liste des maillots");
		MenuItemModel menuItem2 = new MenuItemModel(1, "Commande", "mes maillots");
		MenuItemModel menuItem3 = new MenuItemModel(1, "Historique", "mes commandes");
		
		List<MenuItemModel> menuItems = new ArrayList<MenuItemModel>();
		menuItems.add(menuItem1);
		menuItems.add(menuItem2);
		menuItems.add(menuItem3);
		return menuItems;
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		initMenu(true);
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			toggleMenu();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if (mIsLayoutShown) {
			toggleMenu();
		} else {
			super.onBackPressed();
		}
	}

	public void toggleMenu() {

		switch (mType) {
		case MENU_TYPE_SLIDEOVER:
			toggleSlideOverMenu();
			break;
		case MENU_TYPE_PARALLAX:
			toggleSlidingMenu(mAnimationDuration / 2);
			break;
		default: /* MENU_TYPE_SLIDING */
			toggleSlidingMenu();
			break;
		}
	}

	public void toggleSlideOverMenu() {

		View v2 = findViewById(R.id.ws_munday_slidingmenu_content_frame);
		v2.clearAnimation();
		v2.setDrawingCacheEnabled(true);

		if (mIsLayoutShown) {
			MarginAnimation a = new MarginAnimation(v2, mMenuWidth, 0,
					mInterpolator);
			a.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					ViewGroup v1 = (ViewGroup) findViewById(R.id.ws_munday_slidingmenu_menu_frame);
					v1.setVisibility(View.GONE);
				}
			});

			a.setDuration(mAnimationDuration);
			v2.startAnimation(a);
		} else {
			MarginAnimation a = new MarginAnimation(v2, 0, mMenuWidth,
					mInterpolator);

			a.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
					ViewGroup v1 = (ViewGroup) findViewById(R.id.ws_munday_slidingmenu_menu_frame);
					v1.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
				}
			});

			a.setDuration(mAnimationDuration);
			v2.startAnimation(a);
		}

		mIsLayoutShown = !mIsLayoutShown;

	}

	public void toggleSlidingMenu() {
		toggleSlidingMenu(mAnimationDuration);
	}

	public void toggleSlidingMenu(long menuAnimationDuration) {

		boolean parallax = menuAnimationDuration != mAnimationDuration;

		View v2 = findViewById(R.id.ws_munday_slidingmenu_content_frame);
		v2.clearAnimation();
		v2.setDrawingCacheEnabled(true);

		View vMenu = findViewById(R.id.ws_munday_slidingmenu_menu_frame);
		vMenu.clearAnimation();
		vMenu.setDrawingCacheEnabled(true);

		if (mIsLayoutShown) {

			MarginAnimation a = new MarginAnimation(v2, mMenuWidth, 0,
					mInterpolator);
			a.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					ViewGroup v1 = (ViewGroup) findViewById(R.id.ws_munday_slidingmenu_menu_frame);
					v1.setVisibility(View.GONE);
				}
			});

			a.setDuration(menuAnimationDuration);
			v2.startAnimation(a);

			if (parallax) {
				MarginAnimation a2 = new MarginAnimation(vMenu, 0, -mMenuWidth,
						mInterpolator);
				a2.setDuration(mAnimationDuration);
				vMenu.startAnimation(a2);
			}
		} else {

			MarginAnimation a = new MarginAnimation(v2, 0, mMenuWidth,
					mInterpolator);
			a.setAnimationListener(new AnimationListener() {
				public void onAnimationStart(Animation animation) {
					ViewGroup v1 = (ViewGroup) findViewById(R.id.ws_munday_slidingmenu_menu_frame);
					v1.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
				}
			});

			a.setDuration(mAnimationDuration);
			v2.startAnimation(a);

			if (parallax) {
				MarginAnimation a2 = new MarginAnimation(vMenu, -mMenuWidth, 0,
						mInterpolator);
				a2.setDuration(menuAnimationDuration);
				vMenu.startAnimation(a2);
			}
		}

		mIsLayoutShown = !mIsLayoutShown;

	}

	public void initMenu(boolean isConfigChange) {

		switch (mType) {

		case MENU_TYPE_SLIDEOVER:
			initSlideOverMenu(isConfigChange);
			break;

		default:
			initSlideOutMenu(isConfigChange);
			break;

		}
	}

	@SuppressWarnings("deprecation")
	public void initSlideOutMenu(boolean isConfigChange) {
		// get menu and main layout
		FrameLayout menu = (FrameLayout) findViewById(R.id.ws_munday_slidingmenu_menu_frame);
		FrameLayout root = (FrameLayout) findViewById(R.id.ws_munday_slidingmenu_content_frame);

		// get screen width
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		int x = 0;
		try {
			Method m = Display.class.getMethod("getSize", new Class[] {});
			m.invoke(display, size);
			x = size.x;
		} catch (NoSuchMethodException nsme) {
			x = display.getWidth();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		// make sure that the content doesn't slide all the way off screen
		int minContentWidth = Utility.dipsToPixels(this, mMinMainWidthDps);
		mMenuWidth = Math.min(x - minContentWidth, mMaxMenuWidthDps);

		// update sizes and margins for sliding menu
		RelativeLayout.LayoutParams mp = new RelativeLayout.LayoutParams(
				mMenuWidth, RelativeLayout.LayoutParams.MATCH_PARENT);
		RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(x,
				RelativeLayout.LayoutParams.MATCH_PARENT);

		if (isConfigChange) {
			if (mIsLayoutShown) {
				mp.leftMargin = 0;
				rp.leftMargin = mMenuWidth;
				rp.rightMargin = -mMenuWidth;
			} else {
				mp.leftMargin = -mMenuWidth;
				rp.leftMargin = 0;
				rp.rightMargin = 0;
			}
		} else {
			mp.leftMargin = -mMenuWidth;
			rp.leftMargin = 0;
			rp.rightMargin = -mMenuWidth;
			mIsLayoutShown = false;
		}

		menu.setLayoutParams(mp);
		menu.requestLayout();

		root.setLayoutParams(rp);
		root.requestLayout();
	}

	public boolean ismIsLayoutShown() {
		return mIsLayoutShown;
	}

	@SuppressWarnings("deprecation")
	public void initSlideOverMenu(boolean isConfigChange) {
		// get menu and main layout
		ViewGroup menu = (ViewGroup) findViewById(R.id.ws_munday_slidingmenu_menu_frame);
		// ViewGroup content = (ViewGroup)
		// findViewById(R.id.ws_munday_slidingmenu_content_frame);

		// get screen width
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		int x = 0;
		try {
			Method m = Display.class.getMethod("getSize", new Class[] {});
			m.invoke(display, size);
			x = size.x;
		} catch (NoSuchMethodException nsme) {
			x = display.getWidth();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		// make sure that the content doesn't slide all the way off screen
		int minContentWidth = Utility.dipsToPixels(this, mMinMainWidthDps);
		mMenuWidth = Math.min(x - minContentWidth, mMaxMenuWidthDps);

		// update sizes and margins for sliding menu
		menu.setLayoutParams(new RelativeLayout.LayoutParams(mMenuWidth,
				RelativeLayout.LayoutParams.MATCH_PARENT));
		// menu.requestLayout();

		if (isConfigChange) {
			mIsLayoutShown = !mIsLayoutShown;
			toggleMenu();
		}
	}
}