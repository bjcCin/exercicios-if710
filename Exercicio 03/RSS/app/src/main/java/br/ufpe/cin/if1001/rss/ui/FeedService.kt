package br.ufpe.cin.if1001.rss.ui

import android.app.IntentService
import android.content.Intent
import android.support.annotation.Nullable
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import br.ufpe.cin.if1001.rss.domain.ItemRSS
import br.ufpe.cin.if710.rss.ParserRSS
import br.ufpe.cin.if710.rss.SQLiteRSSHelper
import org.xmlpull.v1.XmlPullParserException
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL



class FeedService : IntentService("FeedService") {

    private val db = SQLiteRSSHelper.getInstance(this)
    val FEED_LOADED = "FeedService.LOADED"
    val INSERTED_DATA = "FeedService.INSERTED"



    //Sem isso o manifest xml não deixa adicionar o service
    override fun onCreate() {
        super.onCreate()
    }

    override fun onHandleIntent(@Nullable intent: Intent?) {
        Log.i("Entrou no OnHandle", "OnHandle")
        assert(intent != null)
        val feeds = intent?.getStringExtra("link")
        Log.i("Criou", feeds)
        val items: List<ItemRSS>
        try {
            val feed = getRssFeed(feeds!!)
            items = ParserRSS.parse(feed)
            for (i in items) {
                Log.d("DB", "Buscando no Banco por link: " + i.link)
                val item = db.getItemRSS(i.link)
                if (item == null) {
                    sendBroadcast(Intent(INSERTED_DATA))
                    Log.d("DB", "Encontrado pela primeira vez: " + i.title)
                    db.insertItem(i)

                }
            }

            LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(FEED_LOADED))

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: XmlPullParserException) {
            e.printStackTrace()
        } finally {
            db.close()
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
}