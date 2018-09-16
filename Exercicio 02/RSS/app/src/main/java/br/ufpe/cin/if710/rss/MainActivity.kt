package br.ufpe.cin.if710.rss

import android.app.Activity
import android.os.Bundle
import android.support.v7.util.SortedList
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : Activity() {

    //ao fazer envio da resolucao, use este link no seu codigo!
    private var RSS_FEED:String = "";

    //OUTROS LINKS PARA TESTAR...
    //http://rss.cnn.com/rss/edition.rss
    //http://pox.globo.com/rss/g1/brasil/
    //http://pox.globo.com/rss/g1/ciencia-e-saude/
    //http://pox.globo.com/rss/g1/tecnologia/

    //use ListView ao invés de TextView - deixe o atributo com o mesmo nome
    private var conteudoRSS: RecyclerView? = null

    //Classe que implementa o Recycle View
    private var itemAdapter: ItemAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main);
        //Carregando o endereco de feed a partir do xml de strings
        RSS_FEED = getString(R.string.rssfeed)
        conteudoRSS = findViewById(R.id.conteudoRSS)

        val linearLayoutManager = LinearLayoutManager(this)
        conteudoRSS?.layoutManager = linearLayoutManager






    }

    override fun onStart() {
        super.onStart()
        try {
            // Realizando o AyncTask com o doAsync do Anko
            doAsync {
                // Faz a requisicao e executa o parse
                val feedXML = ParserRSS.parse(getRssFeed(RSS_FEED))

                uiThread {
                    //  Cria o Adpter a partir do parser feito no xml
                    itemAdapter = ItemAdapter(feedXML)
                    // Adiciona o adpter ao recycle view
                    conteudoRSS?.adapter = itemAdapter
                }
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    //Opcional - pesquise outros meios de obter arquivos da internet - bibliotecas, etc.
    @Throws(IOException::class)
    private fun getRssFeed(feed: String): String {
        var `in`: InputStream? = null
        var rssFeed = ""
        try {
            val url = URL(feed)
            val conn = url.openConnection() as HttpURLConnection
            `in` = conn.inputStream
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var count: Int = `in`.read(buffer)
            while (count != -1) {
                out.write(buffer, 0, count)
                count = `in`.read(buffer)
            }
            val response = out.toByteArray()
            rssFeed = String(response, charset("UTF-8"))
        } finally {
            `in`?.close()
        }
        return rssFeed
    }



}