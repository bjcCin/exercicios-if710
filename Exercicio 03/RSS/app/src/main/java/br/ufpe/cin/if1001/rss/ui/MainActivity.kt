package br.ufpe.cin.if1001.rss.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.*

import org.xmlpull.v1.XmlPullParserException

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

import br.ufpe.cin.if1001.rss.R
import br.ufpe.cin.if1001.rss.domain.ItemRSS
import br.ufpe.cin.if710.rss.ParserRSS
import br.ufpe.cin.if710.rss.SQLiteRSSHelper


class MainActivity : Activity() {

    private var conteudoRSS: ListView? = null
    private val RSS_FEED = "http://rss.cnn.com/rss/edition.rss"
    private var db: SQLiteRSSHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = SQLiteRSSHelper.getInstance(this)

        conteudoRSS = findViewById(R.id.conteudoRSS)

        val adapter = SimpleCursorAdapter(
                //contexto, como estamos acostumados
                this,
                //Layout XML de como se parecem os itens da lista
                R.layout.item, null,
                //Mapeamento das colunas nos IDs do XML.
                // Os dois arrays a seguir devem ter o mesmo tamanho
                arrayOf(SQLiteRSSHelper.ITEM_TITLE, SQLiteRSSHelper.ITEM_DATE),
                intArrayOf(R.id.itemTitulo, R.id.itemData),
                //Flags para determinar comportamento do adapter, pode deixar 0.
                0
        )//Objeto do tipo Cursor, com os dados retornados do banco.
        //Como ainda não fizemos nenhuma consulta, está nulo.
        //Seta o adapter. Como o Cursor é null, ainda não aparece nada na tela.
        conteudoRSS!!.adapter = adapter

        // permite filtrar conteudo pelo teclado virtual
        conteudoRSS!!.isTextFilterEnabled = true

        //Complete a implementação deste método de forma que ao clicar, o link seja aberto no navegador e
        // a notícia seja marcada como lida no banco
        conteudoRSS!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->

            val adapter = parent.adapter as SimpleCursorAdapter
            val mCursor = adapter.getItem(position) as Cursor
            val link = mCursor.getString(4)

            if(db!!.markAsRead(link)){
                //Abrir o brownser do celular aqui
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                parent.context.startActivity(browserIntent)
            }

            else Toast.makeText(applicationContext, "Aconteceu algum problema", Toast.LENGTH_SHORT).show()

        }
    }

    override fun onStart() {
        super.onStart()
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val linkfeed = preferences.getString("rssfeedlink", resources.getString(R.string.rssfeed))
        Log.i("Service", "Entrou no Feed")

        //carrega a classe service
        val carregarFeed = Intent(this, FeedService::class.java)
        carregarFeed.putExtra("link", linkfeed)

        Toast.makeText(applicationContext, "Aguardando o service...", Toast.LENGTH_LONG).show()
        startService(carregarFeed)
            //Agora é feito através de um service
        //CarregaRSS().execute(linkfeed)
    }

    override fun onResume() {
        super.onResume()
        val f = IntentFilter("FeedService.LOADED")
        //Broadcast que irá receber a mensagem e carregar as notícias na tela
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(feedCarregado, f)
    }

    private val feedCarregado = object : BroadcastReceiver() {
        override fun onReceive(ctxt: Context, i: Intent) {
            ExibirFeed().execute()
            Toast.makeText(applicationContext, "Feed Carregado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        db!!.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }


    internal inner class CarregaRSS : AsyncTask<String, Void, Boolean>() {

        override fun doInBackground(vararg feeds: String): Boolean? {
            var flag_problema = false
            var items: List<ItemRSS>? = null
            try {
                val x = feeds[0]
                val feed = getRssFeed(x)
                items = ParserRSS.parse(feed)

                for (i in items) {
                    Log.d("DB", "Buscando no Banco por link: " + i.link)
                    val item = db?.getItemRSS(i.link)
                    if (item == null) {
                        Log.d("DB", "Encontrado pela primeira vez: " + i.title)
                        db!!.insertItem(i)
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
                flag_problema = true
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
                flag_problema = true
            }

            return flag_problema
        }

        override fun onPostExecute(teveProblema: Boolean?) {
            if (teveProblema!!) {
                Toast.makeText(this@MainActivity, "Houve algum problema ao carregar o feed.", Toast.LENGTH_SHORT).show()
            } else {
                //dispara o task que exibe a lista
                ExibirFeed().execute()
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class ExibirFeed : AsyncTask<Void, Void, Cursor>() {

        override fun doInBackground(vararg voids: Void): Cursor? {

            val c = db!!.getItems()
            return c
        }

        override fun onPostExecute(c: Cursor?) {
            if (c != null) {
                (conteudoRSS!!.adapter as CursorAdapter).changeCursor(c)
            }
        }
    }

    @Throws(IOException::class)
    fun getRssFeed(feed: String): String {
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
                count = `in`.read(buffer)            }
            val response = out.toByteArray()
            rssFeed = String(response, charset("UTF-8"))
        } finally {
            `in`?.close()
        }
        return rssFeed
    }

    fun teste (x: Int){
        var y = x
    }
}
