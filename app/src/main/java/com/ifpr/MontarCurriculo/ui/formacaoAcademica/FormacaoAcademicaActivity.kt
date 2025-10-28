package com.ifpr.MontarCurriculo.ui.formacaoAcademica

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.MontarCurriculo.R
import com.ifpr.MontarCurriculo.baseclasses.CurriculoModel
import com.ifpr.MontarCurriculo.baseclasses.Formacao
import com.ifpr.MontarCurriculo.ui.curriculo.VisualizarCurriculoActivity


class FormacaoAcademicaActivity : AppCompatActivity() {

    private lateinit var editTextInstituicao: EditText
    private lateinit var editTextCurso: EditText
    private lateinit var editTextPeriodoFormacao: EditText
    private lateinit var editTextNivel: EditText
    private lateinit var buttonAdicionarFormacao: Button
    private lateinit var buttonFinalizarFormacoes: Button

    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var currentCurriculoId: String? = null
    private var currentCurriculo: CurriculoModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formacao)

        // Inicializar views
        editTextInstituicao = findViewById(R.id.editTextInstituicao)
        editTextCurso = findViewById(R.id.editTextCurso)
        editTextPeriodoFormacao = findViewById(R.id.editTextPeriodoFormacao)
        editTextNivel = findViewById(R.id.editTextNivel)
        buttonAdicionarFormacao = findViewById(R.id.buttonAdicionarFormacao)
        buttonFinalizarFormacoes = findViewById(R.id.buttonFinalizarFormacoes)

        // Configurar Firebase
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuário não logado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("curriculos")

        // Obter ID do currículo da activity anterior
        currentCurriculoId = intent.getStringExtra("curriculoId")
        if (currentCurriculoId == null) {
            Toast.makeText(this, "Erro: ID do currículo não fornecido.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Carregar dados do currículo
        loadCurriculoData(userId, currentCurriculoId!!)

        // Configurar listeners dos botões
        buttonAdicionarFormacao.setOnClickListener {
            adicionarFormacao(isFinalizing = false)
        }

        buttonFinalizarFormacoes.setOnClickListener {
            // Se tiver algo preenchido, adiciona antes de finalizar
            if (camposPreenchidos()) {
                adicionarFormacao(isFinalizing = true)
            }
            // Sempre permite finalizar (não obrigatório)
            navigateToNextStep()
        }
    }

    private fun loadCurriculoData(userId: String, curriculoId: String) {
        databaseReference.child(curriculoId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    currentCurriculo = snapshot.getValue(CurriculoModel::class.java)
                    Log.d("FormacaoActivity", "Currículo carregado: ${currentCurriculo?.id}")
                } else {
                    Log.w("FormacaoActivity", "Currículo não encontrado para ID: $curriculoId. Criando novo.")
                    currentCurriculo = CurriculoModel(id = curriculoId, userId = userId)
                    // Salvar o novo currículo no Firebase
                    saveCurriculoToFirebase(currentCurriculo!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FormacaoActivity", "Erro ao carregar currículo: ${error.message}")
                Toast.makeText(this@FormacaoAcademicaActivity, "Erro ao carregar dados do currículo.", Toast.LENGTH_SHORT).show()
                currentCurriculo = CurriculoModel(id = curriculoId, userId = userId)
            }
        })
    }

    private fun camposPreenchidos(): Boolean {
        val instituicao = editTextInstituicao.text.toString().trim()
        val curso = editTextCurso.text.toString().trim()
        val periodo = editTextPeriodoFormacao.text.toString().trim()
        val nivel = editTextNivel.text.toString().trim()

        
        return true
      //  return instituicao.isNotEmpty() || curso.isNotEmpty() || periodo.isNotEmpty() || nivel.isNotEmpty()
    }

    private fun adicionarFormacao(isFinalizing: Boolean = false) {
        val instituicao = editTextInstituicao.text.toString().trim()
        val curso = editTextCurso.text.toString().trim()
        val periodo = editTextPeriodoFormacao.text.toString().trim()
        val nivel = editTextNivel.text.toString().trim()

        // Se todos os campos estão vazios, não adiciona nada (mas também não mostra erro)
        if (instituicao.isEmpty() && curso.isEmpty() && periodo.isEmpty() && nivel.isEmpty()) {
            return
        }

        // Criar nova formação
        val novaFormacao = Formacao(
            instituicao = if (instituicao.isNotEmpty()) instituicao else null,
            curso = if (curso.isNotEmpty()) curso else null,
            //periodo = if (periodo.isNotEmpty()) periodo else null,
            //nivel = if (nivel.isNotEmpty()) nivel else null
        )

        currentCurriculo?.let { curriculo ->
            // Adicionar à lista de formações
            val formacoesMutavel = curriculo.formacoes?.toMutableList() ?: mutableListOf()
            formacoesMutavel.add(novaFormacao)
            curriculo.formacoes = formacoesMutavel

            // Limpar campos e mostrar feedback
            clearInputFields()
            Toast.makeText(this, "Formação adicionada com sucesso!", Toast.LENGTH_SHORT).show()

            // Salvar no Firebase
            saveCurriculoToFirebase(curriculo)

        } ?: run {
            Toast.makeText(this, "Erro: Currículo não disponível.", Toast.LENGTH_LONG).show()
        }
    }

    private fun clearInputFields() {
        editTextInstituicao.text.clear()
        editTextCurso.text.clear()
        editTextPeriodoFormacao.text.clear()
        editTextNivel.text.clear()
    }

    private fun saveCurriculoToFirebase(curriculo: CurriculoModel) {
        currentCurriculoId?.let { id ->
            databaseReference.child(id).setValue(curriculo)
                .addOnSuccessListener {
                    Log.d("FormacaoActivity", "Currículo salvo no Firebase com sucesso.")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("FormacaoActivity", "Erro ao salvar currículo: ${e.message}")
                }
        }
    }

    private fun navigateToNextStep() {
        val intent = Intent(this, VisualizarCurriculoActivity::class.java)
        intent.putExtra("curriculoId", currentCurriculoId)
        startActivity(intent)
        finish()
    }
}
