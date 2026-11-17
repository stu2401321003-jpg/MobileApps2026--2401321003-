package com.example.whattowear_project.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.whattowear_project.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * Начален екран: тук ще показваме днешното време и препоръчания тоалет.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    // ViewBinding: _binding живее само докато съществува изгледът на фрагмента.
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Освобождаваме изгледа, за да няма memory leak.
        _binding = null
    }
}
