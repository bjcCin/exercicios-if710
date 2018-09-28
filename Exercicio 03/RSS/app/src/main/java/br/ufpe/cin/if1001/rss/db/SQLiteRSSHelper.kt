package br.ufpe.cin.if710.rss

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import br.ufpe.cin.if1001.rss.db.RssProviderContract
import br.ufpe.cin.if1001.rss.domain.ItemRSS


class SQLiteRSSHelper private constructor(internal var c: Context) : SQLiteOpenHelper(c, DATABASE_NAME, null, DB_VERSION) {

    @Throws(SQLException::class)
    fun getItems(): Cursor {
        val selectQuery = "select * from $DATABASE_TABLE where $ITEM_UNREAD = 0"

        val db_read = this.readableDatabase
        return db_read.rawQuery(selectQuery, null)
    }

    var db:  SQLiteDatabase? = null

    override fun onCreate(db: SQLiteDatabase) {
        //Executa o comando de criação de tabela
        db.execSQL(CREATE_DB_COMMAND)
        this.db = db
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //estamos ignorando esta possibilidade no momento
        throw RuntimeException("nao se aplica")
    }

    //IMPLEMENTAR ABAIXO
    //Implemente a manipulação de dados nos métodos auxiliares para não ficar criando consultas manualmente
    fun insertItem(item: ItemRSS): Long {
        return insertItem(item.title, item.pubDate, item.description, item.link)
    }

    fun insertItem(title: String, pubDate: String, description: String, link: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(ITEM_TITLE, title)
        values.put(ITEM_LINK, link)
        values.put(ITEM_DATE, pubDate)
        values.put(ITEM_DESC, description)
        values.put(ITEM_UNREAD, "0")
        //Insere o intem na database
        db.insertWithOnConflict(DATABASE_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        return 0
    }

    @Throws(SQLException::class)
    fun getItemRSS(link: String): ItemRSS? {

        val db_item = this.readableDatabase
        val columns = arrayOf(ITEM_TITLE, ITEM_LINK, ITEM_DATE, ITEM_DESC, ITEM_UNREAD)
        val selection = "$ITEM_LINK = ?"
        val selectionArgs = arrayOf(link)
        val c = db_item.query(DATABASE_TABLE, columns, selection, selectionArgs, null, null, null)
        var aux: ItemRSS? = null

        while (c.moveToNext()) {
            val title = c.getString(c.getColumnIndexOrThrow(ITEM_TITLE))
            val date = c.getString(c.getColumnIndexOrThrow(ITEM_DATE))
            val desc = c.getString(c.getColumnIndexOrThrow(ITEM_DESC))
            aux = ItemRSS(title, link, date, desc)
        }

        return aux
    }

    fun markAsUnread(link: String): Boolean {
        return false
    }

    fun markAsRead(link: String): Boolean {
        val db = this.writableDatabase

        val values = ContentValues()
        val item = getItemRSS(link)
        values.put(ITEM_TITLE, item?.title)
        values.put(ITEM_LINK, item?.link)
        values.put(ITEM_DATE, item?.pubDate)
        values.put(ITEM_DESC, item?.description)
        values.put(ITEM_UNREAD, "1")

        val whereClause = "$ITEM_LINK = ?"

        val aux = db.update(DATABASE_TABLE, values, whereClause, arrayOf(link))

        return aux == 1
    }

    companion object {
        //Nome do Banco de Dados
        private val DATABASE_NAME = "rss"
        //Nome da tabela do Banco a ser usada
        val DATABASE_TABLE = "items"
        //Versão atual do banco
        private val DB_VERSION = 1

        private var db: SQLiteRSSHelper? = null

        //Definindo Singleton
        fun getInstance(c: Context): SQLiteRSSHelper {
            if (db == null) {
                db = SQLiteRSSHelper(c.applicationContext)
            }
            return db as SQLiteRSSHelper
        }

        //Definindo constantes que representam os campos do banco de dados
        val ITEM_ROWID = RssProviderContract._ID
        val ITEM_TITLE = RssProviderContract.TITLE
        val ITEM_DATE = RssProviderContract.DATE
        val ITEM_DESC = RssProviderContract.DESCRIPTION
        val ITEM_LINK = RssProviderContract.LINK
        val ITEM_UNREAD = RssProviderContract.UNREAD

        //Definindo constante que representa um array com todos os campos
        val columns = arrayOf<String>(ITEM_ROWID, ITEM_TITLE, ITEM_DATE, ITEM_DESC, ITEM_LINK, ITEM_UNREAD)

        //Definindo constante que representa o comando de criação da tabela no banco de dados
        private val CREATE_DB_COMMAND = "CREATE TABLE " + DATABASE_TABLE + " (" +
                ITEM_ROWID + " integer primary key autoincrement, " +
                ITEM_TITLE + " text not null, " +
                ITEM_DATE + " text not null, " +
                ITEM_DESC + " text not null, " +
                ITEM_LINK + " text not null, " +
                ITEM_UNREAD + " boolean not null);"
    }

}
