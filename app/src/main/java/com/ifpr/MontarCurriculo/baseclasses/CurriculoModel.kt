package com.ifpr.MontarCurriculo.baseclasses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CurriculoModel(
    var id: String? = null, // ID único do currículo (usado como chave no Firebase)
    var userId: String? = null, // UID do usuário que criou o currículo
    var nomeCompleto: String? = null,
    var endereco: String? = null,
    var idade: String? = null, // Mantendo como String para flexibilidade (ex: "22 anos")
    var telefone: String? = null,
    var email: String? = null,
    var linkedIn: String? = null,
    var emailContato: String? = null, // Para evitar conflito com o email de login, se for diferente
    var objetivoProfissional: String? = null,
    var experiencias: List<Experiencia>? = null, // Lista de experiências profissionais
    var formacoes: List<Formacao>? = null // Lista de formações acadêmicas
) : Parcelable

@Parcelize
data class Experiencia(
    var empresa: String? = null,
    var cargo: String? = null,
    var periodo: String? = null, // Ex: "012025 - 032025"
    var descricao: String? = null,
    var principaisAtividades: List<String>? = null // Ex: lista de bullet points
) : Parcelable

@Parcelize
data class Formacao(
    var instituicao: String? = null,
    var curso: String? = null,
    var anoConclusao: String? = null // Ex: "2026" ou "N/A"
) : Parcelable