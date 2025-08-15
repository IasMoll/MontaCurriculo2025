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
import com.ifpr.MontarCurriculo.ui.curriculo.VisualizarCurriculoActivity // Próxima tela

class FormacaoAcademicaActivity : AppCompatActivity() {

    // Conecta as caixas de texto e botões do XML ao código
    private lateinit var editTextInstituicao: EditText
    private lateinit var editTextCurso: EditText
    private lateinit var editTextPeriodoFormacao: EditText
    private lateinit var editTextNivel: EditText
    private lateinit var buttonAdicionarFormacao: Button
    private lateinit var buttonFinalizarFormacoes: Button

    // Para se comunicar com o Firebase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // Para guardar os dados do currículo enquanto estamos trabalhando nele
    private var currentCurriculoId: String? = null
    private var currentCurriculo: CurriculoModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Diz à Activity qual layout ela deve usar
        setContentView(R.layout.activity_formacao) // Certifique-se de que este é o layout correto

        // Encontra os elementos do layout pelos seus IDs
        editTextInstituicao = findViewById(R.id.editTextInstituicao)
        editTextCurso = findViewById(R.id.editTextCurso)
        editTextPeriodoFormacao = findViewById(R.id.editTextPeriodoFormacao)
        editTextNivel = findViewById(R.id.editTextNivel)
        buttonAdicionarFormacao = findViewById(R.id.buttonAdicionarFormacao)
        buttonFinalizarFormacoes = findViewById(R.id.buttonFinalizarFormacoes)

        // Configura o Firebase para o usuário logado
        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuário não logado.", Toast.LENGTH_SHORT).show()
            finish() // Fecha a tela se não houver usuário
            return
        }
        // Caminho no Firebase: users -> [ID do Usuário] -> curriculos
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("curriculos")

        // Pega o ID do currículo que foi passado da tela anterior (ExperienciaProfissionalActivity)
        currentCurriculoId = intent.getStringExtra("curriculoId")
        if (currentCurriculoId == null) {
            Toast.makeText(this, "Erro: ID do currículo não fornecido.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Carrega os dados do currículo do Firebase (se já existirem)
        loadCurriculoData(userId, currentCurriculoId!!)

        // O que acontece quando os botões são clicados
        buttonAdicionarFormacao.setOnClickListener {
            // Tenta adicionar a formação. Se os campos estiverem vazios, não adiciona e mostra um Toast.
            adicionarFormacao(isFinalizing = false)
        }

        buttonFinalizarFormacoes.setOnClickListener {
            // Tenta adicionar a formação atual ANTES de finalizar.
            // Se os campos estiverem vazios, ela simplesmente não adicionará nada e prosseguirá.
            adicionarFormacao(isFinalizing = true)
            navigateToNextStep() // Vai para a próxima tela
        }
    }

    // Função para carregar os dados do currículo do Firebase
    private fun loadCurriculoData(userId: String, curriculoId: String) {
        databaseReference.child(curriculoId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Se o currículo existir, carrega ele para nossa variável 'currentCurriculo'
                    currentCurriculo = snapshot.getValue(CurriculoModel::class.java)
                } else {
                    // Se não existir (algo deu errado ou é um novo currículo), cria um novo CurriculoModel
                    Log.w("FormacaoActivity", "Currículo não encontrado para ID: $curriculoId. Iniciando novo CurriculoModel.")
                    Toast.makeText(this@FormacaoAcademicaActivity, "Currículo não encontrado. Iniciando novo.", Toast.LENGTH_SHORT).show()
                    currentCurriculo = CurriculoModel(id = curriculoId, userId = userId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FormacaoActivity", "Erro ao carregar currículo: ${error.message}")
                Toast.makeText(this@FormacaoAcademicaActivity, "Erro ao carregar dados do currículo.", Toast.LENGTH_SHORT).show()
                // Em caso de erro, ainda tentamos criar um CurriculoModel para evitar NullPointerException
                currentCurriculo = CurriculoModel(id = curriculoId, userId = userId)
            }
        })
    }

    // Função para adicionar uma formação à lista do currículo
    // isFinalizing: true se a chamada veio do botão "Finalizar", false se veio do "Adicionar Formação"
    private fun adicionarFormacao(isFinalizing: Boolean = false) {
        // Pega os textos digitados nos campos
        val instituicao = editTextInstituicao.text.toString().trim()
        val curso = editTextCurso.text.toString().trim()
        val periodo = editTextPeriodoFormacao.text.toString().trim()
        val nivel = editTextNivel.text.toString().trim()

        // *** Lógica para tornar a formação acadêmica não obrigatória ***
        // Verifica se *todos* os campos de formação estão vazios
        if (instituicao.isEmpty() && curso.isEmpty() && periodo.isEmpty() && nivel.isEmpty()) {
            if (!isFinalizing) { // Se não estamos finalizando, avisamos que nada será adicionado
                Toast.makeText(this, "Nenhuma informação de formação para adicionar. Preencha ou finalize.", Toast.LENGTH_SHORT).show()
            }
            // Se todos os campos estiverem vazios, não há nada para adicionar, então apenas sai da função.
            // Isso permite que o botão "Finalizar" prossiga sem erros, mesmo com campos vazios.
            return
        }

        // Se chegamos até aqui, significa que pelo menos um campo foi preenchido, então vamos criar a Formacao
        val novaFormacao = Formacao(
            instituicao = instituicao,
            curso = curso,
            //periodo = periodo, // MANTENHA ESTA LINHA DESCOMENTADA
           // nivel = nivel      // MANTENHA ESTA LINHA DESCOMENTADA
        )

        // Adiciona a nova formação à lista do currículo
        currentCurriculo?.let { curriculo ->
            // Pega a lista de formações, se não existir, cria uma nova lista mutável
            val formacoesMutavel = curriculo.formacoes?.toMutableList() ?: mutableListOf()
            formacoesMutavel.add(novaFormacao) // Adiciona a nova formação
            curriculo.formacoes = formacoesMutavel // Atualiza a lista no currículo

            // Limpa os campos da tela para o usuário poder adicionar outra formação
            clearInputFields()
            Toast.makeText(this, "Formação adicionada! Adicione mais ou finalize.", Toast.LENGTH_SHORT).show()

            // Salva o currículo atualizado no Firebase
            saveCurriculoToFirebase(curriculo)

        } ?: run {
            Toast.makeText(this, "Erro: Currículo não disponível para adicionar formação.", Toast.LENGTH_LONG).show()
        }
    }

    // Função para limpar os campos após adicionar uma formação
    private fun clearInputFields() {
        editTextInstituicao.text.clear()
        editTextCurso.text.clear()
        editTextPeriodoFormacao.text.clear()
        editTextNivel.text.clear()
    }

    // Função para salvar o currículo atualizado no Firebase
    private fun saveCurriculoToFirebase(curriculo: CurriculoModel) {
        currentCurriculoId?.let { id ->
            databaseReference.child(id).setValue(curriculo) // Salva o objeto completo
                .addOnSuccessListener {
                    Log.d("FormacaoActivity", "Currículo atualizado no Firebase com nova formação.")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao salvar formação: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("FormacaoActivity", "Erro ao salvar formação: ${e.message}")
                }
        }
    }

    // Função para ir para a próxima tela (VisualizarCurriculoActivity)
    private fun navigateToNextStep() {
        val intent = Intent(this, VisualizarCurriculoActivity::class.java) // Diz para qual tela ir
        intent.putExtra("curriculoId", currentCurriculoId) // Passa o ID do currículo adiante
        startActivity(intent) // Inicia a próxima tela
        finish() // Fecha a tela atual para não voltar para ela
    }
}