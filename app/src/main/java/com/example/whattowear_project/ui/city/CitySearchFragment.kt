package com.example.whattowear_project.ui.city

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.whattowear_project.R
import com.example.whattowear_project.databinding.FragmentCitySearchBinding
import com.example.whattowear_project.ui.common.applyBottomSystemInsets
import com.example.whattowear_project.viewmodel.CitySearchState
import com.example.whattowear_project.viewmodel.CitySearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/** „Избери град“: търсене в Open-Meteo по име, избор с едно натискане. */
@AndroidEntryPoint
class CitySearchFragment : Fragment() {

    private var _binding: FragmentCitySearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CitySearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCitySearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerCities.applyBottomSystemInsets()

        val adapter = CityAdapter { city ->
            viewModel.selectCity(city)
            findNavController().navigateUp()
        }
        binding.recyclerCities.adapter = adapter

        binding.editSearch.doAfterTextChanged { viewModel.onQueryChanged(it?.toString().orEmpty()) }

        // При първо отваряне - фокус в полето и клавиатура, за да се пише веднага.
        if (savedInstanceState == null) {
            binding.editSearch.requestFocus()
            ViewCompat.getWindowInsetsController(binding.editSearch)?.show(WindowInsetsCompat.Type.ime())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state, adapter) }
            }
        }
    }

    private fun render(state: CitySearchState, adapter: CityAdapter) {
        binding.progress.visibility = if (state is CitySearchState.Loading) View.VISIBLE else View.INVISIBLE

        val cities = (state as? CitySearchState.Results)?.cities.orEmpty()
        adapter.submitList(cities)
        binding.recyclerCities.isVisible = cities.isNotEmpty()

        val message = when (state) {
            CitySearchState.Idle -> R.string.city_search_idle
            CitySearchState.NothingFound -> R.string.city_search_nothing_found
            CitySearchState.Error -> R.string.error_no_connection
            else -> null
        }
        binding.layoutMessage.isVisible = message != null
        if (message != null) binding.textMessage.setText(message)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
