package com.taller2.linearlayouts.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.taller2.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.taller2.linearlayouts.SupabaseCliente
import com.taller2.linearlayouts.data.UsuarioRepository
import com.taller2.linearlayouts.ui.auth.LoginActivity
import com.taller2.linearlayouts.ui.main.admin.AdminFragment
import com.taller2.linearlayouts.ui.main.admin.UsuariosFragment
import com.taller2.linearlayouts.ui.main.perfil.EditarPerfilFragment
import com.taller2.linearlayouts.ui.main.perfil.PerfilFragment
import com.taller2.linearlayouts.ui.main.productos.CarritoFragment
import com.taller2.linearlayouts.ui.main.productos.CatalogoFragment
import com.taller2.linearlayouts.ui.main.productos.HomeFragment
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.black)

        // Cargar Fragmento inicial
        if (savedInstanceState == null) {
            cargarFragment(HomeFragment())
            bottomNav.selectedItemId = R.id.inicio
        }

        configurarMenuPorRol(navView.menu)

        // Listener del Menu Inferior (IDs de bottom_nav_menu.xml)
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

        // Listener del Menu Lateral
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.inicio -> {
                    cargarFragment(HomeFragment())
                    bottomNav.selectedItemId = R.id.inicio
                }
                R.id.catalogoProductos -> {
                    cargarFragment(CatalogoFragment())
                    bottomNav.selectedItemId = R.id.catalogoProductos
                }
                R.id.carritoCompras -> {
                    cargarFragment(CarritoFragment())
                    bottomNav.selectedItemId = R.id.carritoCompras
                }
                R.id.miPerfil -> {
                    cargarFragment(PerfilFragment())
                    bottomNav.selectedItemId = R.id.miPerfil
                }
                R.id.editarPerfil -> {
                    cargarFragment(EditarPerfilFragment())
                    bottomNav.selectedItemId = R.id.editarPerfil
                }
                R.id.crear_productos -> cargarFragment(AdminFragment())
                R.id.crear_usuarios -> cargarFragment(UsuariosFragment())
                R.id.cerrarsesion -> cerrarSesion()
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun configurarMenuPorRol(menu: Menu) {
        lifecycleScope.launch {
            try {
                val rol = UsuarioRepository.obtenerRolActual()
                runOnUiThread {
                    when (rol) {
                        "admin" -> {
                            menu.findItem(R.id.crear_productos)?.isVisible = true
                            menu.findItem(R.id.crear_usuarios)?.isVisible = true
                        }
                        "vendedor" -> {
                            menu.findItem(R.id.crear_productos)?.isVisible = true
                            menu.findItem(R.id.crear_usuarios)?.isVisible = false
                        }
                        else -> { // cliente
                            menu.findItem(R.id.crear_productos)?.isVisible = false
                            menu.findItem(R.id.crear_usuarios)?.isVisible = false
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MAIN_ACTIVITY", "Error rol: ${e.message}")
            }
        }
    }

    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun cerrarSesion() {
        lifecycleScope.launch {
            try {
                SupabaseCliente.client.auth.signOut()
                runOnUiThread {
                    startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    finishAffinity()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error al cerrar sesión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}