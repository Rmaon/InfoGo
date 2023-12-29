package com.ramon.infogo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> CrearIncidenciaFragment()
            1 -> IncidenciaListFragment()
            2 -> SettingsFragment()
            else -> throw IllegalArgumentException("Invalid position $position")
        }
    }

    override fun getCount(): Int {
        return 3 // Número total de Fragmentos
    }

    override fun getPageTitle(position: Int): CharSequence? {
        // Puedes establecer títulos para cada página si lo deseas
        return when (position) {
            0 -> "Crear Incidencia"
            1 -> "Ver Incidencias"
            2 -> "Ajustes"
            else -> null
        }
    }
}
