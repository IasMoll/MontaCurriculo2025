package com.ifpr.MontarCurriculo.ui.curriculo // Pacote onde sua Activity está localizada

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ifpr.MontarCurriculo.R // Importa R para acessar seus recursos (layouts, drawables, IDs)

class DadosPessoaisActivity : AppCompatActivity() {

    // Declarar as views que você vai interagir
    private lateinit var imageViewFotoPerfil: ImageView
    private lateinit var editTextNomeCompleto: EditText
    private lateinit var editTextEndereco: EditText
    private lateinit var editTextIdade: EditText
    private lateinit var buttonProximoDadosPessoais: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.informacoes_pessoais) // <-- Garante que carrega o layout correto

        // 1. Inicializar as views
        imageViewFotoPerfil = findViewById(R.id.imageViewFotoPerfil)
        editTextNomeCompleto = findViewById(R.id.editTextNomeCompleto)
        editTextEndereco = findViewById(R.id.editTextEndereco)
        editTextIdade = findViewById(R.id.editTextIdade)
        buttonProximoDadosPessoais = findViewById(R.id.buttonProximoDadosPessoais)

        // 2. Adicionar listeners ou lógica (exemplo)

        // Exemplo: Ao clicar na imagem de perfil, abre uma galeria/câmera (lógica a ser implementada depois)
        imageViewFotoPerfil.setOnClickListener {
            Toast.makeText(this, "Abrir galeria ou câmera para foto", Toast.LENGTH_SHORT).show()
            // TODO: Implementar lógica para seleção/tirar foto
        }

        // Exemplo: Ao clicar no botão "Próximo"
        buttonProximoDadosPessoais.setOnClickListener {
            // Pegar os valores dos campos
            val nomeCompleto = editTextNomeCompleto.text.toString().trim()
            val endereco = editTextEndereco.text.toString().trim()
            val idadeStr = editTextIdade.text.toString().trim()

            // Validar campos (exemplo simples)
            if (nomeCompleto.isEmpty() || endereco.isEmpty() || idadeStr.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios (*)", Toast.LENGTH_LONG).show()
            } else {
                val idade = idadeStr.toIntOrNull()
                if (idade == null || idade <= 0) {
                    Toast.makeText(this, "Por favor, insira uma idade válida.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Dados Pessoais Salvos! Indo para a próxima tela.", Toast.LENGTH_SHORT).show()
                    // TODO: Salvar os dados (banco de dados, SharedPreferences, etc.)
                    // TODO: Navegar para a próxima Activity (ex: ExperienciaProfissionalActivity)
                    // val intent = Intent(this, ExperienciaProfissionalActivity::class.java)
                    // startActivity(intent)
                }
            }
        }
    }
}