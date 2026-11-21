package com.example.whattowear_project.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.whattowear_project.R
import com.example.whattowear_project.data.entity.ClothingType
import com.example.whattowear_project.databinding.FragmentHomeBinding
import com.example.whattowear_project.databinding.ItemOutfitPickBinding
import com.example.whattowear_project.outfit.Outfit
import com.example.whattowear_project.outfit.OutfitPick
import com.example.whattowear_project.outfit.PickReason
import com.example.whattowear_project.ui.common.NavArgs
import com.example.whattowear_project.ui.common.heroBackgroundRes
import com.example.whattowear_project.ui.common.iconRes
import com.example.whattowear_project.ui.common.labelRes
import com.example.whattowear_project.ui.common.showClothingImage
import com.example.whattowear_project.viewmodel.HomeViewModel
import com.example.whattowear_project.viewmodel.OutfitUiState
import com.example.whattowear_project.viewmodel.WeatherUiState
import com.example.whattowear_project.weather.model.Weather
import com.example.whattowear_project.weather.model.WeatherCondition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Начален екран: днешното време в избрания град + препоръчан тоалет от гардероба.
 * Портретно - едно под друго; хоризонтално - времето вляво, тоалетът вдясно (layout-land).
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    // ViewBinding: _binding живее само докато съществува изгледът на фрагмента.
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val weather = binding.weather

        // Чипът с града и стрелката ▾ до него отварят избора на град.
        weather.chipCity.setOnClickListener { openCitySearch() }
        weather.chipCity.setOnCloseIconClickListener { openCitySearch() }
        weather.chipCity.closeIconContentDescription = getString(R.string.change_city)
        weather.buttonRefresh.setOnClickListener { viewModel.refresh() }
        binding.outfit.buttonGoToWardrobe.setOnClickListener { openWardrobeTab() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.weather.collect { state -> renderWeather(state) } }
                launch { viewModel.outfit.collect { state -> renderOutfit(state) } }
            }
        }
    }

    // ---------------------------------------------------------------- Времето

    private fun renderWeather(state: WeatherUiState) {
        val card = binding.weather
        card.progressWeather.isVisible = state is WeatherUiState.Loading
        card.layoutWeather.isVisible = state is WeatherUiState.Success
        card.layoutHeroMessage.isVisible = state is WeatherUiState.NoCity || state is WeatherUiState.Error
        card.buttonRefresh.isVisible = state is WeatherUiState.Success

        when (state) {
            WeatherUiState.NoCity -> {
                card.heroBackground.setBackgroundResource(R.drawable.bg_hero_day)
                card.chipCity.setText(R.string.choose_city)
                card.textHeroMessage.setText(R.string.weather_no_city)
                card.buttonHeroAction.setText(R.string.choose_city)
                card.buttonHeroAction.setIconResource(R.drawable.ic_location)
                card.buttonHeroAction.setOnClickListener { openCitySearch() }
            }
            is WeatherUiState.Loading -> card.chipCity.text = state.city.name
            is WeatherUiState.Error -> {
                card.heroBackground.setBackgroundResource(R.drawable.bg_hero_cloudy)
                card.chipCity.text = state.city.name
                card.textHeroMessage.setText(R.string.weather_error)
                card.buttonHeroAction.setText(R.string.retry)
                card.buttonHeroAction.setIconResource(R.drawable.ic_refresh)
                card.buttonHeroAction.setOnClickListener { viewModel.refresh() }
            }
            is WeatherUiState.Success -> showWeather(state.weather)
        }
    }

    private fun showWeather(weather: Weather) {
        val card = binding.weather
        card.heroBackground.setBackgroundResource(weather.heroBackgroundRes)
        card.chipCity.text = weather.city.name
        card.textTemperature.text = getString(R.string.temp_value, weather.temperature)
        card.textCondition.setText(weather.condition.labelRes)
        card.textFeelsLike.text = getString(R.string.weather_feels_like, weather.feelsLike)
        card.imageWeather.setImageResource(weather.condition.iconRes(weather.isDay))
        card.textMinMax.text = getString(R.string.weather_min_max, weather.minTemperature, weather.maxTemperature)
        card.textRainChance.text = getString(R.string.weather_rain_chance, weather.precipitationProbability)
        card.textWind.text = getString(R.string.weather_wind, weather.windSpeed)
    }

    // ---------------------------------------------------------------- Тоалетът

    private fun renderOutfit(state: OutfitUiState) {
        val section = binding.outfit
        section.layoutOutfit.isVisible = state is OutfitUiState.Ready
        section.cardOutfitMessage.isVisible = state !is OutfitUiState.Ready
        section.buttonGoToWardrobe.isVisible = state is OutfitUiState.EmptyWardrobe

        when (state) {
            OutfitUiState.WaitingForWeather -> section.textOutfitMessage.setText(R.string.outfit_waiting_for_weather)
            OutfitUiState.EmptyWardrobe -> section.textOutfitMessage.setText(R.string.outfit_empty_wardrobe)
            is OutfitUiState.Ready -> showOutfit(state.outfit, state.weather)
        }
    }

    private fun showOutfit(outfit: Outfit, weather: Weather) {
        val section = binding.outfit

        // „Днес е 3° и вали.“
        val headline = when {
            weather.condition == WeatherCondition.SNOW -> R.string.outfit_headline_snow
            outfit.isRainy -> R.string.outfit_headline_rain
            else -> R.string.outfit_headline
        }
        section.textOutfitHeadline.text = getString(headline, outfit.temperature)

        // „Облечи: Черно яке + Дънки + Ботуши + Чадър“
        section.textOutfitSummary.text = if (outfit.picks.isEmpty()) {
            getString(R.string.outfit_nothing_fits, outfit.temperature)
        } else {
            getString(
                R.string.outfit_summary,
                outfit.picks.joinToString(getString(R.string.outfit_separator)) { it.item.name },
            )
        }

        // Карта за всяка дреха.
        section.containerPicks.removeAllViews()
        outfit.picks.forEach { pick -> section.containerPicks.addView(createPickView(pick)) }

        // Съвети: какво липсва.
        section.containerTips.removeAllViews()
        tipsFor(outfit).forEach { text ->
            val tip = layoutInflater.inflate(R.layout.item_outfit_tip, section.containerTips, false) as TextView
            tip.text = text
            section.containerTips.addView(tip)
        }
    }

    private fun createPickView(pick: OutfitPick): View {
        val row = ItemOutfitPickBinding.inflate(layoutInflater, binding.outfit.containerPicks, false)
        val item = pick.item

        showClothingImage(item, row.thumbContainer, row.thumbTypeIcon, row.thumbPhoto)
        row.textPickName.text = item.name
        val kind = getString(item.subtype?.labelRes ?: item.type.labelRes)
        row.textPickDetails.text = "$kind · ${getString(R.string.temp_range, item.minTemp, item.maxTemp)}"

        // Причина - само при специално правило.
        val reason = when (pick.reason) {
            PickReason.COLD_NEEDS_JACKET -> R.string.outfit_reason_cold to R.drawable.ic_snowflake_small
            PickReason.RAIN_PROTECTION -> R.string.outfit_reason_rain to R.drawable.ic_water_drop_small
            PickReason.FITS_TEMPERATURE -> null
        }
        row.textPickReason.isVisible = reason != null
        if (reason != null) {
            row.textPickReason.setText(reason.first)
            row.textPickReason.setCompoundDrawablesRelativeWithIntrinsicBounds(reason.second, 0, 0, 0)
        }

        row.root.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_detail, bundleOf(NavArgs.ITEM_ID to item.id))
        }
        return row.root
    }

    private fun tipsFor(outfit: Outfit): List<String> = buildList {
        if (outfit.missingJacket) add(getString(R.string.outfit_tip_missing_jacket))
        if (outfit.missingRainProtection) add(getString(R.string.outfit_tip_missing_rain))
        outfit.missingTypes.forEach { type ->
            val text = when (type) {
                ClothingType.TOP -> R.string.outfit_tip_missing_top
                ClothingType.BOTTOM -> R.string.outfit_tip_missing_bottom
                ClothingType.SHOES -> R.string.outfit_tip_missing_shoes
                else -> null
            }
            if (text != null) add(getString(text, outfit.temperature))
        }
    }

    // ---------------------------------------------------------------- Навигация

    private fun openCitySearch() {
        findNavController().navigate(R.id.action_home_to_citySearch)
    }

    /** Превключва на таба „Гардероб“ - така, както го прави долната лента. */
    private fun openWardrobeTab() {
        val navController = findNavController()
        navController.navigate(
            R.id.wardrobeFragment,
            null,
            navOptions {
                launchSingleTop = true
                restoreState = true
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            },
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Освобождаваме изгледа, за да няма memory leak.
        _binding = null
    }
}
