package org.andcreator.iconpack.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.graphics.Color
import android.os.*
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import org.andcreator.iconpack.R
import org.andcreator.iconpack.fragment.*

import kotlinx.android.synthetic.main.activity_main.*
import org.andcreator.iconpack.adapter.PagerBarAdapter
import org.andcreator.iconpack.bean.PagerBarBean
import org.andcreator.iconpack.util.*
import org.andcreator.iconpack.view.NavigationPagerBar
import java.io.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: SectionsPagerAdapter
    private var requestsFragment: RequestFragment? = null
    private var iconsFragment: IconsFragment? = null
    private var homeFragment: HomeFragment? = null
    private var applyFragment: ApplyFragment? = null
    private var aboutFragment: AboutFragment? = null
    private var tabTextColor = 0xfffff
    private var isDark = false

    private val mHandler = @SuppressLint("HandlerLeak")
    object : Handler(Looper.getMainLooper()){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what){
                1 -> {
                    updateContent()
                }
                2 ->{
                    updateContent()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isDark = ContextCompat.getColor(this, R.color.backgroundColor) == ContextCompat.getColor(this, R.color.white)
        setThemeDefault(isDark)

        setContentView(R.layout.activity_main)
        checkVersion()
        initView()

    }

    private fun initView(){

        AppAdaptationHelper.setContext(this).getAppFilterError {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("????????????")
                .setMessage("appfilter.xml ??????????????????????????????\n${it}")
                .setPositiveButton("????????????") { _, _ ->
                    Utils.copy(it, this@MainActivity)
                }.setNeutralButton(resources.getString(R.string.cancel)){_,_->

                }.show()
        }

        tabTextColor = ContextCompat.getColor(this@MainActivity,R.color.white)

        adapter = SectionsPagerAdapter(supportFragmentManager)
        setupViewPager(pager)
        pager.offscreenPageLimit = 5

        pagerPointBars.setAdapter(getPagerBarAdapter())
        pagerPointBars.setClickListener(object : NavigationPagerBar.OnItemClickListener{
            override fun onClick(position: Int) {
                pager.currentItem = position
            }
        })

        pagerPointBars.onAnimationOffset(0)

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageSelected(p0: Int) {
                pagerPointBars.onAnimation(p0)
                if (p0!= 4){
                    setThemeDefault(isDark)
                }else{
                    if (aboutFragment!!.isDark){
                        setTheme(true)
                    }else{
                        setTheme(false)
                    }
                }
            }

        })
    }

    private fun setTheme(isDark: Boolean){

        if (isDark){
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }else{
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_VISIBLE or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }

    private fun setThemeDefault(isDark: Boolean){

        if(isDark){

            // Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show()
            // ?????????????????????????????????????????????
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
            }
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.WHITE

            //API???????????????Android NavigationBar Color
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                window.navigationBarColor = ContextCompat.getColor(this, R.color.backgroundColor)
            }else{
                window.navigationBarColor = Color.BLACK
            }
        }else {

            // Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show()
            // ?????????????????????????????????????????????
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_VISIBLE)
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.BLACK

            //API???????????????Android NavigationBar Color
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                window.navigationBarColor = ContextCompat.getColor(this, R.color.backgroundColor)
            }else{
                window.navigationBarColor = Color.BLACK
            }
        }
    }

    /**
     * ?????????????????????????????????
     */
    private fun checkVersion() {

        doAsyncTask{

            //??????????????????????????????
            if (!PreferencesUtil.get(this@MainActivity, "first", false)){
                PreferencesUtil.put(this@MainActivity, "first", true)
                PreferencesUtil.put(this@MainActivity, "versionCode", Utils.getAppVersion(this@MainActivity))

                val file = File(filesDir, "appfilter.xml")
                val files = File(file.parent)
                files.mkdirs()
                file.createNewFile()
                val msg = Message()
                msg.what = 1
                mHandler.sendMessage(msg)

                //??????????????????
                AppAdaptationHelper.setContext(this).getAppFilterListener {
                    saveAppFilter(it)
                }
            }

            //??????????????????????????????
            if (PreferencesUtil.get(this@MainActivity, "versionCode", -1) != -1 && PreferencesUtil.get(this@MainActivity, "versionCode", -1) != Utils.getAppVersion(this@MainActivity)){

                //????????????????????????????????????????????????????????????
                try {
                    if (File(filesDir, "appfilter.xml").delete()){
                        val files = File(filesDir, "appfilter.xml")
                        File(filesDir, "appfilter-new.xml").renameTo(files)
                    }
                }catch (e: IOException){

                }

                //???????????????????????????(??????????????????)
                AppAdaptationHelper.setContext(this).getAppFilterListener {
                    saveAppFilter(it)
                }

                val msg = Message()
                msg.what = 2
                mHandler.sendMessage(msg)
                PreferencesUtil.put(this@MainActivity, "versionCode", Utils.getAppVersion(this@MainActivity))
            }else{
                AppAdaptationHelper.setContext(this)
            }
        }
    }

    /**
     * ?????????????????????
     */
    private fun updateContent() {
        UpdateFragment().show(supportFragmentManager,"UpdateDialog")
    }

    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        homeFragment?.setCallbackListener(object : HomeFragment.Callbacks{
            override fun callback(position: Int) {
                when(position) {
                    1 -> pager.currentItem = 1
                    2 -> updateContent()
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        homeFragment?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupViewPager(viewPager: ViewPager) {
        homeFragment = HomeFragment()
        iconsFragment = IconsFragment()
        requestsFragment = RequestFragment()
        applyFragment = ApplyFragment()
        aboutFragment = AboutFragment()
        homeFragment?.retainInstance = true
        iconsFragment?.retainInstance = true
        requestsFragment?.retainInstance = true
        applyFragment?.retainInstance = true
        aboutFragment?.retainInstance = true

        homeFragment?.setCallbackListener(object : HomeFragment.Callbacks{
            override fun callback(position: Int) {
                when(position) {
                    1 -> pager.currentItem = 1
                    2 -> updateContent()
                }
            }
        })

        requestsFragment?.setCallbackListener(object : RequestFragment.Callbacks{
            override fun callback(position: Int) {
                when (position) {
                    0 -> hide(pagerPointBar)
                    1 -> show(pagerPointBar)
                    2 -> SnackbarUtil.snackbarUtil(this@MainActivity, pagerPointBar ,resources.getString(R.string.no_choose_app))
                }
            }
        })

        iconsFragment?.setCallbackListener(object : IconsFragment.Callbacks{
            override fun callback(position: Int) {
                when(position) {
                    0 -> hide(pagerPointBar)
                    1 -> show(pagerPointBar)
                }
            }
        })

        adapter.addFragment(homeFragment!!, resources.getString(R.string.home))
        adapter.addFragment(iconsFragment!!, resources.getString(R.string.icons))
        adapter.addFragment(requestsFragment!!, resources.getString(R.string.icon_adapter))
        adapter.addFragment(applyFragment!!, resources.getString(R.string.apply))
        adapter.addFragment(aboutFragment!!, resources.getString(R.string.about))
        viewPager.adapter = adapter
    }

    /**
     * A [FragmentPagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter (fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }

        fun getFragment(position: Int): Fragment {
            return mFragmentList[position]
        }
    }

    /**
     * ??????Tab????????????
     */
    private fun getPagerBarAdapter(): PagerBarAdapter{

        return object : PagerBarAdapter() {

            override fun getHolder(): ArrayList<PagerBarBean> {
                val items  = ArrayList<PagerBarBean>()
                items.add(PagerBarBean(R.drawable.ic_home_24px, ContextCompat.getColor(this@MainActivity, R.color.colorAccent), resources.getString(R.string.home),
                    isIconAnim = false,
                    isTitleAnim = false
                ))
                items.add(PagerBarBean(R.drawable.ic_twotone_dashboard_24px, ContextCompat.getColor(this@MainActivity, R.color.colorAccent), resources.getString(R.string.icons),
                    isIconAnim = false,
                    isTitleAnim = false
                ))
                items.add(PagerBarBean(R.drawable.ic_category_24px, ContextCompat.getColor(this@MainActivity, R.color.colorAccent), resources.getString(R.string.icon_adapter),
                    isIconAnim = false,
                    isTitleAnim = false
                ))
                items.add(PagerBarBean(R.drawable.ic_style_24px, ContextCompat.getColor(this@MainActivity, R.color.colorAccent), resources.getString(R.string.apply),
                    isIconAnim = false,
                    isTitleAnim = false
                ))
                items.add(PagerBarBean(R.drawable.ic_emoji_events_24px, ContextCompat.getColor(this@MainActivity, R.color.colorAccent),resources.getString(R.string.about),
                    isIconAnim = false,
                    isTitleAnim = false
                ))
                return items
            }

            override fun createMenuItem(parent: ViewGroup, index: Int): View {
                return LayoutInflater.from(parent.context).inflate(R.layout.item_pagerbar, parent, false)
            }

            override fun getCount(): Int {
                return 5
            }

        }
    }

    /**
     * ???????????????
     */
    private fun show(view: View) {
        view.animate().cancel()

        view.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(200)
            .setInterpolator(LinearOutSlowInInterpolator())
            .setListener(object : AnimatorListenerAdapter() {

                override fun onAnimationStart(animation: Animator) {
                    view.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {

                }
            })
    }

    /**
     * ???????????????
     */
    private fun hide(view: View) {
        view.animate().cancel()
        view.animate()
            .translationY(view.height.toFloat())
            .alpha(0f)
            .setDuration(200)
            .setInterpolator(FastOutLinearInInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                private var mCancelled: Boolean = false

                override fun onAnimationStart(animation: Animator) {
                    view.visibility = View.VISIBLE
                    mCancelled = false
                }

                override fun onAnimationCancel(animation: Animator) {
                    mCancelled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (!mCancelled) {
                        view.visibility = View.INVISIBLE
                    }
                }
            })
    }

    /**
     * ??????????????????appfilter
     */
    private fun saveAppFilter(appFilter: String) {

        val file = File(filesDir, "appfilter-new.xml")
        var out: FileOutputStream? = null
        try {
            if (!file.exists()) {
                val files = File(file.parent)
                files.mkdirs()
                file.createNewFile()
            }

            out = FileOutputStream(file,false)
            out.write(appFilter.toByteArray())

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                out?.flush()
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    override fun onDestroy() {
        Glide.get(this).clearMemory()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (iconsFragment != null && iconsFragment!!.cancelPreview()) {

        } else if (pager.currentItem != 0){
            pager.currentItem = 0
        }else{
            super.onBackPressed()
        }
    }
}
