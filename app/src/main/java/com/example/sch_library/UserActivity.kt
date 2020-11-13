package com.example.sch_library

import android.os.Bundle
<<<<<<< HEAD
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
=======
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
>>>>>>> 72ce08b77bda28f29da7a4371941cf529d43e961
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView

class UserActivity : AppCompatActivity() {
<<<<<<< HEAD
=======
    lateinit var searchView: SearchView
>>>>>>> 72ce08b77bda28f29da7a4371941cf529d43e961
    lateinit var viewPager: ViewPager
    lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val fragmentHome = HomeFragment()
        val fragmentBasket = BasketFragment()
        val fragmentInfoManage = InfoManageFragment()
        val fragmentLogout = LogoutFragment()

<<<<<<< HEAD
        val fm = supportFragmentManager
        val adapter = ViewPagerAdapter(fm)
=======
        searchView = findViewById(R.id.searchview)

        viewPager = findViewById(R.id.viewpager)
        viewPager.offscreenPageLimit = 4
        val adapter = ViewPagerAdapter(supportFragmentManager)
>>>>>>> 72ce08b77bda28f29da7a4371941cf529d43e961
        adapter.addItem(fragmentHome)
        adapter.addItem(fragmentBasket)
        adapter.addItem(fragmentInfoManage)
        adapter.addItem(fragmentLogout)
<<<<<<< HEAD

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
=======
        viewPager.adapter = adapter
>>>>>>> 72ce08b77bda28f29da7a4371941cf529d43e961

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
<<<<<<< HEAD
                else -> false
            }
        }
    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
=======
            }
            false
        }
    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
>>>>>>> 72ce08b77bda28f29da7a4371941cf529d43e961
        private val items = ArrayList<Fragment>()

        fun addItem(item: Fragment) { items.add(item) }

        override fun getCount(): Int = items.size
        override fun getItem(position: Int): Fragment = items[position]
    }
}