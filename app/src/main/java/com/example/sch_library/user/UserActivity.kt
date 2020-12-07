package com.example.sch_library.user

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.sch_library.LogoutFragment
import com.example.sch_library.R
import com.google.android.material.bottomnavigation.BottomNavigationView

lateinit var fragmentInfoManage: InfoManageFragment
lateinit var fragmentOrderDetails: OrderDetailsFragment
lateinit var fragmentHome: UserHomeFragment
lateinit var fragmentBasket: BasketFragment
lateinit var fragmentLogout: LogoutFragment

var userId = ""

class UserActivity : AppCompatActivity(), OnSetHomeViewAdapterListener {
    private lateinit var viewPager: ViewPager
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var homeViewAdapter: UserHomeFragment.HomeViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        val intent = intent
        userId = intent.getStringExtra("id")
        val pw = intent.getStringExtra("pw")
        val name = intent.getStringExtra("name")

        fragmentInfoManage = InfoManageFragment()
        fragmentOrderDetails = OrderDetailsFragment()
        fragmentHome = UserHomeFragment()
        fragmentBasket = BasketFragment()
        fragmentLogout = LogoutFragment()

        val fm = supportFragmentManager
        val adapter = ViewPagerAdapter(fm)
        adapter.addItem(fragmentInfoManage)
        adapter.addItem(fragmentOrderDetails)
        adapter.addItem(fragmentHome)
        adapter.addItem(fragmentBasket)
        adapter.addItem(fragmentLogout)

        viewPager = findViewById<ViewPager>(R.id.viewpager).apply {
            offscreenPageLimit = 5
            setAdapter(adapter)
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) { }
                override fun onPageSelected(position: Int) {
                    when (position) {
                        0 -> bottomNavigationView.menu.findItem(R.id.menu_info_manage).isChecked = true
                        1 -> bottomNavigationView.menu.findItem(R.id.menu_order_details).isChecked = true
                        2 -> bottomNavigationView.menu.findItem(R.id.menu_home).isChecked = true
                        3 -> bottomNavigationView.menu.findItem(R.id.menu_basket).isChecked = true
                        4 -> bottomNavigationView.menu.findItem(R.id.menu_logout).isChecked = true
                    }
                }
                override fun onPageScrollStateChanged(state: Int) { }
            })
        }

        bottomNavigationView = findViewById(R.id.bottomnavigationview)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_info_manage -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.menu_order_details -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.menu_home -> {
                    viewPager.currentItem = 2
                    true
                }
                R.id.menu_basket -> {
                    viewPager.currentItem = 3
                    true
                }
                R.id.menu_logout -> {
                    viewPager.currentItem = 4
                    true
                }
                else -> false
            }
        }

        viewPager.currentItem = 2
    }

    override fun onSetHomeViewAdapter(viewAdapter: UserHomeFragment.HomeViewAdapter) {
        homeViewAdapter = viewAdapter
    }

    private fun updateHomeViewAdapter() {
        if (homeViewAdapter.getSelectedCount() ==  0) {
            viewPager.currentItem = 4
        } else {
            homeViewAdapter.clear()
            homeViewAdapter.notifyDataSetChanged()
        }
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 2) {
            updateHomeViewAdapter()
        } else {
            viewPager.currentItem = 4
        }
    }

    class ViewPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val items = ArrayList<Fragment>()

        fun addItem(item: Fragment) { items.add(item) }

        override fun getCount(): Int = items.size
        override fun getItem(position: Int): Fragment = items[position]
    }
}