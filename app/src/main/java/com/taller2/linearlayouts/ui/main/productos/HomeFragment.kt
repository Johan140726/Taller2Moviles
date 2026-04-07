package com.taller2.linearlayouts.ui.main.productos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taller2.R

class HomeFragment : Fragment() {

    private val listaProductos = listOf(
        Product("Camisa Casual", 29.9, R.drawable.red_shirt),
        Product("Camisa Sport", 39.9, R.drawable.red_shirt),
        Product("Pantalon Jean", 49.9, R.drawable.red_shirt),
        Product("Zapatos Deportivos", 59.9, R.drawable.red_shirt),
        )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
    val view = inflater.inflate(R.layout.fragment_home, container, false)
    val recyclerView=view.findViewById<RecyclerView>(R.id.recycler_productos)

        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = ProductoAdapter(listaProductos)

        return view

    }


}
