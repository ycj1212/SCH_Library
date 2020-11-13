package com.example.sch_library

import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserActivity : AppCompatActivity() {
    lateinit var viewPager: ViewPager
    lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val fragmentHome = HomeFragment()
        val fragmentBasket = BasketFragment()
        val fragmentInfoManage = InfoManageFragment()
        val fragmentLogout = LogoutFragment()

        val fm = supportFragmentManager
        val adapter = ViewPagerAdapter(fm)
        adapter.addItem(fragmentHome)
        adapter.addItem(fragmentBasket)
        adapter.addItem(fragmentInfoManage)
        adapter.addItem(fragmentLogout)

        viewPager = findViewById<ViewPager>(R.id.viewpager).apply {
            offscreenPageLimit = 4
            setAdapter(adapter)
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) { }
                override fun onPageSelected(position: Int) {
                    when (position) {
                        0 -> bottomNavigationView.menu.findItem(R.id.menu_home).isChecked = true
                        1 -> bottomNavigationView.menu.findItem(R.id.menu_basket).isChecked = true
                        2 -> bottomNavigationView.menu.findItem(R.id.menu_info_manage).isChecked = true
                        3 -> bottomNavigationView.menu.findItem(R.id.menu_logout).isChecked = true
                    }
                }
                override fun onPageScrollStateChanged(state: Int) { }
            })
        }

        bottomNavigationView = findViewById(R.id.bottomnavigationview)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.menu_basket -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.menu_info_manage -> {
                    viewPager.currentItem = 2
                    true
                }
                R.id.menu_logout -> {
                    viewPager.currentItem = 3
                    true
                }
                else -> false
            }
        }
    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val items = ArrayList<Fragment>()

        fun addItem(item: Fragment) { items.add(item) }

        override fun getCount(): Int = items.size
        override fun getItem(position: Int): Fragment = items[position]
    }
}