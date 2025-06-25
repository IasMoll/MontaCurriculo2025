package com.ifpr.MontarCurriculo.ui.curriculo

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.MontarCurriculo.R
import com.ifpr.MontarCurriculo.baseclasses.CurriculoModel // Importe o CurriculoModel correto
import com.ifpr.MontarCurriculo.baseclasses.Experiencia
import com.ifpr.MontarCurriculo.baseclasses.Formacao
import com.ifpr.MontarCurriculo.ui.dadospessoais.DadosPessoaisActivity // Para o botão de edição
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class VisualizarCurriculoActivity : AppCompatActivity() {

    // Views do layout - Usando os IDs do activity_visualizar_curriculo.xml que te passei
    private lateinit var textViewNomeCompleto: TextView
    private lateinit var textViewIdade: TextView
    private lateinit var textViewNacionalidade: TextView
    private lateinit var textViewEstadoCivil: TextView
    private lateinit var textViewProfissao: TextView
    private lateinit var textViewObjetivo: TextView
    private lateinit var textViewEmail: TextView
    private lateinit var textViewTelefone: TextView
    private lateinit var textViewEndereco: TextView
    private lateinit var layoutExperiencias: LinearLayout // Este é o LinearLayout para adicionar as experiências dinamicamente
    private lateinit var layoutFormacoes: LinearLayout   // Este é o LinearLayout para adicionar as formações dinamicamente
    private lateinit var buttonGerarPDF: Button
    private lateinit var buttonEditarCurriculo: Button

    // Firebase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var currentCurriculoId: String? = null
    private var currentCurriculo: CurriculoModel? = null // Usando o CurriculoModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_curriculo) // Garante que está usando o layout correto

        // Inicializa as views com os IDs CORRETOS do activity_visualizar_curriculo.xml
        textViewNomeCompleto = findViewById(R.id.textViewNomeCompleto)
        textViewIdade = findViewById(R.id.textViewIdade)
        textViewEstadoCivil = findViewById(R.id.textViewEstadoCivil)
        textViewProfissao = findViewById(R.id.textViewProfissao)
        textViewObjetivo = findViewById(R.id.textViewObjetivo)
        textViewEmail = findViewById(R.id.textViewEmail)
        textViewTelefone = findViewById(R.id.textViewTelefone)
        textViewEndereco = findViewById(R.id.textViewEndereco)
        layoutExperiencias = findViewById(R.id.layoutExperiencias) // O LinearLayout para experiências
        layoutFormacoes = findViewById(R.id.layoutFormacoes)     // O LinearLayout para formações
        buttonGerarPDF = findViewById(R.id.buttonGerarPDF)


        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "Usuário não logado.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Caminho no Firebase: users -> [ID do Usuário] -> curriculos
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("curriculos")

        // Pega o ID do currículo que foi passado da tela anterior
        currentCurriculoId = intent.getStringExtra("curriculoId")
        if (currentCurriculoId == null) {
            Toast.makeText(this, "Erro: ID do currículo não fornecido.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Carregar e exibir os dados do currículo do Firebase
        loadAndDisplayCurriculo(userId, currentCurriculoId!!)

        // Configurar listeners dos botões
        buttonGerarPDF.setOnClickListener {
            // A permissão para salvar PDF em Downloads em Android 10+ não requer mais WRITE_EXTERNAL_STORAGE.
            // Para versões mais antigas, precisaríamos de permissão.
            // Por simplicidade, vamos chamar direto a geração, e o sistema cuidará das permissões para Downloads.
            generatePdf()
        }

        buttonEditarCurriculo.setOnClickListener {
            // Volta para a primeira tela de edição (Dados Pessoais)
            val intent = Intent(this, DadosPessoaisActivity::class.java)
            intent.putExtra("curriculoId", currentCurriculoId) // Passa o ID para manter o mesmo currículo
            startActivity(intent)
            finish()
        }
    }

    // Função para carregar os dados do currículo do Firebase
    private fun loadAndDisplayCurriculo(userId: String, curriculoId: String) {
        databaseReference.child(curriculoId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    currentCurriculo = snapshot.getValue(CurriculoModel::class.java)
                    currentCurriculo?.let { displayCurriculo(it) } ?: run {
                        Toast.makeText(this@VisualizarCurriculoActivity, "Erro ao carregar dados do currículo.", Toast.LENGTH_SHORT).show()
                        Log.e("VisualizarCurriculo", "Currículo nulo após carregar do Firebase.")
                    }
                } else {
                    Toast.makeText(this@VisualizarCurriculoActivity, "Currículo não encontrado.", Toast.LENGTH_SHORT).show()
                    Log.e("VisualizarCurriculo", "Snapshot não existe para o ID: $curriculoId")
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@VisualizarCurriculoActivity, "Erro ao carregar currículo: ${error.message}", Toast.LENGTH_LONG).show()
                Log.e("VisualizarCurriculo", "Erro ao carregar currículo: ${error.message}")
                finish()
            }
        })
    }

    // Função para exibir os dados do currículo na tela
    private fun displayCurriculo(curriculo: CurriculoModel) {
        // Exibir Dados Pessoais
        textViewNomeCompleto.text = "Nome: ${curriculo.nomeCompleto ?: "Não informado"}"
        textViewIdade.text = "Idade: ${curriculo.idade?.toString() ?: "Não informado"}"
        textViewNacionalidade.text = "Nacionalidade: ${curriculo.nacionalidade ?: "Não informado"}"
        textViewEstadoCivil.text = "Estado Civil: ${curriculo.estadoCivil ?: "Não informado"}"
        textViewProfissao.text = "Profissão: ${curriculo.profissao ?: "Não informado"}"
        textViewObjetivo.text = "Objetivo: ${curriculo.objetivoProfissional ?: "Não informado"}"

        // Exibir Contato
        textViewEmail.text = "Email: ${curriculo.email ?: "Não informado"}"
        textViewTelefone.text = "Telefone: ${curriculo.telefone ?: "Não informado"}"
        textViewEndereco.text = "Endereço: ${curriculo.endereco ?: "Não informado"}"

        // Limpar layouts de experiências e formações antes de adicionar
        layoutExperiencias.removeAllViews()
        layoutFormacoes.removeAllViews()

        // Exibir Experiências Profissionais
        if (curriculo.experiencias.isNullOrEmpty()) {
            val tv = TextView(this)
            tv.text = "Nenhuma experiência profissional cadastrada."
            tv.setTextColor(ContextCompat.getColor(this, R.color.white)) // Use ContextCompat
            layoutExperiencias.addView(tv)
        } else {
            curriculo.experiencias?.forEach { experiencia ->
                addExperienceView(experiencia)
            }
        }

        // Exibir Formações Acadêmicas
        if (curriculo.formacoes.isNullOrEmpty()) {
            val tv = TextView(this)
            tv.text = "Nenhuma formação acadêmica cadastrada."
            tv.setTextColor(ContextCompat.getColor(this, R.color.white)) // Use ContextCompat
            layoutFormacoes.addView(tv)
        } else {
            curriculo.formacoes?.forEach { formacao ->
                addFormacaoView(formacao)
            }
        }
    }

    // Função auxiliar para adicionar uma view de experiência dinamicamente
    private fun addExperienceView(experiencia: Experiencia) {
        val tv = TextView(this)
        tv.text = """
            Empresa: ${experiencia.empresa}
            Cargo: ${experiencia.cargo}
            Período: ${experiencia.periodo}
            Descrição: ${experiencia.descricao ?: "N/A"}
        """.trimIndent()
        tv.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv.textSize = 14f // Use 'f' para float
        tv.setPadding(0, 8, 0, 8)
        layoutExperiencias.addView(tv)
        // Adiciona uma linha divisória para separar as experiências
        val divider = View(this)
        divider.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1 // 1dp de altura para a linha
        ).apply {
            setMargins(0, 8, 0, 8)
        }
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.gray)) // Use ContextCompat
        layoutExperiencias.addView(divider)
    }

    // Função auxiliar para adicionar uma view de formação dinamicamente
    private fun addFormacaoView(formacao: Formacao) {
        val tv = TextView(this)
        tv.text = """
            Instituição: ${formacao.instituicao}
            Curso: ${formacao.curso}
            Período: ${formacao.periodo}
            Nível: ${formacao.nivel}
        """.trimIndent()
        tv.setTextColor(ContextCompat.getColor(this, R.color.white))
        tv.textSize = 14f // Use 'f' para float
        tv.setPadding(0, 8, 0, 8)
        layoutFormacoes.addView(tv)
        // Adiciona uma linha divisória para separar as formações
        val divider = View(this)
        divider.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            1 // 1dp de altura para a linha
        ).apply {
            setMargins(0, 8, 0, 8)
        }
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.gray)) // Use ContextCompat
        layoutFormacoes.addView(divider)
    }

    private fun generatePdf() {
        val curriculo = currentCurriculo ?: run {
            Toast.makeText(this, "Erro ao gerar PDF: currículo nulo.", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Tamanho A4 (595x842 pontos, 72 dpi)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint() // Usar android.graphics.Paint

        var yPos = 40f // Posição Y inicial para desenhar o texto

        // Título
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Seu Currículo", 200f, yPos, paint)
        yPos += 40f

        // Dados Pessoais
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Dados Pessoais:", 40f, yPos, paint)
        yPos += 25f

        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Nome: ${curriculo.nomeCompleto ?: "N/A"}", 40f, yPos, paint)
        yPos += 20f
        canvas.drawText("Endereço: ${curriculo.endereco ?: "N/A"}", 40f, yPos, paint)
        yPos += 20f
        canvas.drawText("Idade: ${curriculo.idade?.toString() ?: "N/A"}", 40f, yPos, paint)
        yPos += 20f
        canvas.drawText("Estado Civil: ${curriculo.estadoCivil ?: "N/A"}", 40f, yPos, paint)
        yPos += 20f
        canvas.drawText("Profissão: ${curriculo.profissao ?: "N/A"}", 40f, yPos, paint)
        yPos += 20f
        canvas.drawText("Objetivo: ${curriculo.objetivoProfissional ?: "N/A"}", 40f, yPos, paint)
        yPos += 30f

        // Experiências Profissionais
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Experiências Profissionais:", 40f, yPos, paint)
        yPos += 25f

        if (!curriculo.experiencias.isNullOrEmpty()) {
            curriculo.experiencias?.forEach { exp ->
                if (yPos + 80 > pageInfo.pageHeight) { // Verifica se precisa de nova página
                    pdfDocument.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                    val newPage = pdfDocument.startPage(newPageInfo)
                    canvas.set(newPage.canvas) // Redefine o canvas para a nova página
                    yPos = 40f // Reinicia a posição Y
                }
                paint.textSize = 14f
                paint.isFakeBoldText = true
                canvas.drawText("Empresa: ${exp.empresa ?: "N/A"}", 60f, yPos, paint)
                yPos += 20f
                paint.isFakeBoldText = false
                canvas.drawText("Cargo: ${exp.cargo ?: "N/A"}", 80f, yPos, paint)
                yPos += 20f
                canvas.drawText("Período: ${exp.periodo ?: "N/A"}", 80f, yPos, paint)
                yPos += 20f
                if (!exp.descricao.isNullOrEmpty()) {
                    val descriptionLines = splitTextIntoLines(exp.descricao!!, paint, pageInfo.pageWidth - 120)
                    descriptionLines.forEach { line ->
                        canvas.drawText("Descrição: $line", 80f, yPos, paint)
                        yPos += 20f
                    }
                }
                yPos += 15f // Espaço entre experiências
            }
        } else {
            paint.textSize = 14f
            canvas.drawText("Nenhuma experiência informada.", 40f, yPos, paint)
            yPos += 30f
        }

        // Formações Acadêmicas
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Formação Acadêmica:", 40f, yPos, paint)
        yPos += 25f

        if (!curriculo.formacoes.isNullOrEmpty()) {
            curriculo.formacoes?.forEach { form ->
                if (yPos + 80 > pageInfo.pageHeight) { // Verifica se precisa de nova página
                    pdfDocument.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                    val newPage = pdfDocument.startPage(newPageInfo)
                    canvas.set(newPage.canvas) // Redefine o canvas para a nova página
                    yPos = 40f // Reinicia a posição Y
                }
                paint.textSize = 14f
                paint.isFakeBoldText = true
                canvas.drawText("Instituição: ${form.instituicao ?: "N/A"}", 60f, yPos, paint)
                yPos += 20f
                paint.isFakeBoldText = false
                canvas.drawText("Curso: ${form.curso ?: "N/A"}", 80f, yPos, paint)
                yPos += 20f
                canvas.drawText("Período: ${form.periodo ?: "N/A"}", 80f, yPos, paint)
                yPos += 20f
                canvas.drawText("Nível: ${form.nivel ?: "N/A"}", 80f, yPos, paint)
                yPos += 15f // Espaço entre formações
            }
        } else {
            paint.textSize = 14f
            canvas.drawText("Nenhuma formação informada.", 40f, yPos, paint)
            yPos += 30f
        }

        // Dados de Contato
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Dados de Contato:", 40f, yPos, paint)
        yPos += 25f

        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Telefone: ${curriculo.telefone ?: "N/A"}", 40f, yPos, paint)
        yPos += 20f
        canvas.drawText("E-mail: ${curriculo.email ?: "N/A"}", 40f, yPos, paint)
        yPos += 30f


        pdfDocument.finishPage(page)

        // Definir o nome do arquivo
        val nomeArquivo = "Curriculo_${curriculo.nomeCompleto?.replace(" ", "_") ?: "SemNome"}_${System.currentTimeMillis()}.pdf"

        // Obter o diretório de Downloads
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, nomeArquivo)

        try {
            FileOutputStream(file).use { fos ->
                pdfDocument.writeTo(fos)
            }
            Toast.makeText(this, "Currículo salvo em Downloads: $nomeArquivo", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao salvar o PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }

    // Função auxiliar para quebrar texto em várias linhas no PDF
    private fun splitTextIntoLines(text: String, paint: Paint, maxWidth: Int): List<String> {
        val lines = mutableListOf<String>()
        val words = text.split(" ")
        var currentLine = StringBuilder()

        for (word in words) {
            // Verifica se adicionar a próxima palavra excede a largura
            if (paint.measureText(currentLine.toString() + " " + word) < maxWidth) {
                if (currentLine.isNotEmpty()) {
                    currentLine.append(" ")
                }
                currentLine.append(word)
            } else {
                // Adiciona a linha atual e começa uma nova com a palavra atual
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        // Adiciona a última linha se houver conteúdo
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
}