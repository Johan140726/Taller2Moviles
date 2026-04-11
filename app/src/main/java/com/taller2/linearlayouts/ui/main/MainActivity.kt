package com.taller2.linearlayouts.ui.main

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.taller2.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.taller2.linearlayouts.ui.main.perfil.EditarPerfilFragment
import com.taller2.linearlayouts.ui.main.perfil.PerfilFragment
import com.taller2.linearlayouts.ui.main.productos.CarritoFragment
import com.taller2.linearlayouts.ui.main.productos.CatalogoFragment
import com.taller2.linearlayouts.ui.main.productos.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout : androidx.drawerlayout.widget.DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
                drawerLayout=findViewById(R.id.drawer_layout)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this ,
            drawerLayout ,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close


        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        cargarFragment(HomeFragment())
        bottomNav.selectedItemId = R.id.inicio

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.inicio -> cargarFragment(HomeFragment())
                R.id.catalogoProductos -> cargarFragment(CatalogoFragment())
                R.id.carritoCompras -> cargarFragment(CarritoFragment())
                R.id.miPerfil -> cargarFragment(PerfilFragment())
                R.id.editarPerfil -> cargarFragment(EditarPerfilFragment())
            }
            true

        }
        
       navView.setNavigationItemSelectedListener { item ->
           when (item.itemId) {
               R.id.inicio -> cargarFragment(HomeFragment())
               R.id.catalogoProductos -> cargarFragment(CatalogoFragment())
               R.id.carritoCompras -> cargarFragment(CarritoFragment())
               R.id.miPerfil -> cargarFragment(PerfilFragment())
               R.id.editarPerfil -> cargarFragment(EditarPerfilFragment())
           }
           drawerLayout.closeDrawers()
           true

       }

    }
    private fun cargarFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container,fragment)
            .commit()

    }

}