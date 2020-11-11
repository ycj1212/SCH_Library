package com.example.sch_library

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserActivity : AppCompatActivity() {
    lateinit var searchView: SearchView
    lateinit var viewPager: ViewPager
    lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val fragmentHome = HomeFragment()
        val fragmentBasket = BasketFragment()
        val fragmentInfoManage = InfoManageFragment()
        val fragmentLogout = LogoutFragment()

        searchView = findViewById(R.id.searchview)

        viewPager = findViewById(R.id.viewpager)
        viewPager.offscreenPageLimit = 4
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addItem(fragmentHome)
        adapter.addItem(fragmentBasket)
        adapter.addItem(fragmentInfoManage)
        adapter.addItem(fragmentLogout)
        viewPager.adapter = adapter

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
            }
            false
        }
    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val items = ArrayList<Fragment>()

        fun addItem(item: Fragment) { items.add(item) }

        override fun getCount(): Int = items.size
        override fun getItem(position: Int): Fragment = items[position]
    }
}