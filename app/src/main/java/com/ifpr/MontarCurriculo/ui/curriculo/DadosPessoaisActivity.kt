package com.ifpr.MontarCurriculo.ui.dadospessoais // Verifique se o pacote é este ou .ui.curriculo

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
import com.ifpr.MontarCurriculo.ui.curriculo.ContatoActivity

class DadosPessoaisActivity : AppCompatActivity() {

    // Views do layout (usando os IDs que definimos)
    private lateinit var nomeCompletoEditText: EditText
    private lateinit var idadeEditText: EditText
    private lateinit var nacionalidadeEditText: EditText
    private lateinit var estadoCivilEditText: EditText
    private lateinit var profissaoEditText: EditText
    private lateinit var objetivoEditText: EditText
    private lateinit var proximoButton: Button

    // Firebase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var currentCurriculoId: String? = null
    private var currentCurriculo: CurriculoModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.informacoes_pessoais) // Ou R.layout.informacoes_pessoais, dependendo do seu layout principal

        // Inicializa as views com os IDs CORRETOS
        nomeCompletoEditText = findViewById(R.id.editTextNomeCompleto)
        idadeEditText = findViewById(R.id.editTextIdade)


        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuário não logado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        databaseReference = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(userId)
            .child("curriculos")

        // Tenta obter o ID do currículo se veio de outra Activity (ex: VisualizarCurriculoActivity para editar)
        currentCurriculoId = intent.getStringExtra("curriculoId")

        if (currentCurriculoId == null) {
            // Se não há ID, tenta criar um novo currículo com um ID único do Firebase
            currentCurriculoId = databaseReference.push().key
            if (currentCurriculoId == null) {
                Toast.makeText(this, "Erro ao gerar ID para novo currículo.", Toast.LENGTH_LONG).show()
                finish()
                return
            }
            currentCurriculo = CurriculoModel(id = currentCurriculoId!!, userId = userId)
            Log.d("DadosPessoaisActivity", "Novo currículo ID gerado: $currentCurriculoId")
        } else {
            // Se há ID, tenta carregar o currículo existente
            loadCurriculoData(userId, currentCurriculoId!!)
        }

        proximoButton.setOnClickListener {
            saveDadosPessoaisAndNavigate()
        }
    }

    private fun loadCurriculoData(userId: String, curriculoId: String) {
        databaseReference.child(curriculoId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    currentCurriculo = snapshot.getValue(CurriculoModel::class.java)
                    currentCurriculo?.let {
                        nomeCompletoEditText.setText(it.nomeCompleto)
                        idadeEditText.setText(it.idade?.toString())

                        objetivoEditText.setText(it.objetivoProfissional)
                    }
                } else {
                    Log.w("DadosPessoaisActivity", "Currículo não encontrado para edição. Criando um novo.")
                    Toast.makeText(this@DadosPessoaisActivity, "Currículo não encontrado. Iniciando novo.", Toast.LENGTH_SHORT).show()
                    currentCurriculo = CurriculoModel(id = curriculoId, userId = userId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DadosPessoaisActivity", "Erro ao carregar currículo: ${error.message}")
                Toast.makeText(this@DadosPessoaisActivity, "Erro ao carregar dados do currículo.", Toast.LENGTH_SHORT).show()
                currentCurriculo = CurriculoModel(id = curriculoId, userId = userId)
            }
        })
    }

    private fun saveDadosPessoaisAndNavigate() {
        val nomeCompleto = nomeCompletoEditText.text.toString().trim()
        val idadeStr = idadeEditText.text.toString().trim()
        val nacionalidade = nacionalidadeEditText.text.toString().trim()
        val estadoCivil = estadoCivilEditText.text.toString().trim()
        val profissao = profissaoEditText.text.toString().trim()
        val objetivo = objetivoEditText.text.toString().trim()

        if (nomeCompleto.isEmpty() || idadeStr.isEmpty() || nacionalidade.isEmpty() ||
            estadoCivil.isEmpty() || profissao.isEmpty() || objetivo.isEmpty()) {
            Toast.makeText(this, "Todos os campos são obrigatórios.", Toast.LENGTH_SHORT).show()
            return
        }

        val idade = idadeStr.toIntOrNull()
        if (idade == null) {
            Toast.makeText(this, "Idade inválida.", Toast.LENGTH_SHORT).show()
            return
        }

        currentCurriculo?.apply {
            this.nomeCompleto = nomeCompleto
            this.idade = idade.toString()

            this.objetivoProfissional = objetivo
        }

        currentCurriculo?.let { curriculo ->
            currentCurriculoId?.let { id ->
                databaseReference.child(id).setValue(curriculo)
                    .addOnSuccessListener {
                        Log.d("DadosPessoaisActivity", "Dados pessoais salvos/atualizados com sucesso.")
                        val intent = Intent(this, ContatoActivity::class.java)
                        intent.putExtra("curriculoId", id)
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao salvar dados pessoais: ${e.message}", Toast.LENGTH_LONG).show()
                        Log.e("DadosPessoaisActivity", "Erro ao salvar dados pessoais: ${e.message}")
                    }
            }
        } ?: run {
            Toast.makeText(this, "Erro: Currículo não disponível para salvar.", Toast.LENGTH_LONG).show()
        }
    }
}