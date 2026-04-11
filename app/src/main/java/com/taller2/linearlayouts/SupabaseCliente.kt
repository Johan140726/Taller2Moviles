package com.taller2.linearlayouts

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseCliente {
    val client = createSupabaseClient(
        supabaseUrl = "https://gfkpywmixejwmwwrbyby.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imdma3B5d21peGVqd213d3JieWJ5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU2MDE4ODgsImV4cCI6MjA5MTE3Nzg4OH0.dw1uQmWVMwEPOYds82JmF50D05EklgaPsn2PiOuZ1Y0"
    ) {
        install(Postgrest)
        install(Auth)
    }

}