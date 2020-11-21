package com.example.sch_library.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.sch_library.LogoutFragment
import com.example.sch_library.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminActivity : AppCompatActivity() {
    lateinit var viewPager: ViewPager
    lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val fragmentHome = AdminHomeFragment()
        val fragmentPurchaseHistory = PurchaseHistoryFragment()
        val fragmentOrderStatus = OrderStatusFragment()
        val fragmentLogout = LogoutFragment()

        val fm = supportFragmentManager
        val adapter = ViewPagerAdapter(fm)
        adapter.addItem(fragmentHome)
        adapter.addItem(fragmentPurchaseHistory)
        adapter.addItem(fragmentOrderStatus)
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
                        1 -> bottomNavigationView.menu.findItem(R.id.menu_purchace_history).isChecked = true
                        2 -> bottomNavigationView.menu.findItem(R.id.menu_order_status).isChecked = true
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
                R.id.menu_purchace_history -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.menu_order_status -> {
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

    override fun onBackPressed() {
        if (viewAdapter.getSelectedCount() == 0) {
            viewPager.currentItem = 3
        } else {
            viewAdapter.clear()
            viewAdapter.notifyDataSetChanged()
        }
    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val items = ArrayList<Fragment>()

        fun addItem(item: Fragment) { items.add(item) }

        override fun getCount(): Int = items.size
        override fun getItem(position: Int): Fragment = items[position]
    }
}