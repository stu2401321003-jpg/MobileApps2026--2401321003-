package com.example.whattowear_project

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.whattowear_project.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

// @AndroidEntryPoint позволява на Hilt да подава зависимости тук и във фрагментите вътре.
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    /** Главните екрани - на тях има лента с табове и няма стрелка „назад“. */
    private val topLevelDestinations = setOf(R.id.homeFragment, R.id.wardrobeFragment)
    private val appBarConfiguration = AppBarConfiguration(topLevelDestinations)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Отстъп отгоре/отстрани за системните ленти и за отвора на камерата (displayCutout).
        // Долния отстъп го правят BottomNavigationView / NavigationRailView сами.
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            v.setPadding(bars.left, bars.top, bars.right, 0)
            insets
        }

        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupActionBarWithNavController(navController, appBarConfiguration)
        // Портретно има bottomNav, хоризонтално - navRail. Другото е null.
        binding.bottomNav?.setupWithNavController(navController)
        binding.navRail?.setupWithNavController(navController)

        // На вътрешните екрани (напр. „Нова дреха“) скриваме табовете - там само пречат.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = destination.id in topLevelDestinations
            binding.bottomNav?.isVisible = isTopLevel
            binding.navRail?.isVisible = isTopLevel
        }
    }

    /** Стрелката „назад“ в горната лента. */
    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
}
