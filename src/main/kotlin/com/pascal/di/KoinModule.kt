package com.pascal.di

import com.pascal.feature.auth.AuthService
import org.koin.dsl.module

val serviceModule = module {
    single { AuthService() }
}