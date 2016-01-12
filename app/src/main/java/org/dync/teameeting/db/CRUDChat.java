package org.dync.teameeting.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.dync.teameeting.bean.ReqSndMsgEntity;
import org.dync.teameeting.db.chatdao.ChatCacheEntity;
import org.dync.teameeting.db.chatdao.ChatCacheEntityDao;
import org.dync.teameeting.db.chatdao.DaoMaster;
import org.dync.teameeting.db.chatdao.DaoSession;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by zhulang on 2016/1/9 0009.
 */
public class CRUDChat {

    /*******
     * select
     */
    public static List<ChatCacheEntity> selectChatList(Context context, String meetingId) {

        DaoSession session = getSession(context);
        ChatCacheEntityDao chatEnity = session.getChatCacheEntityDao();
        List<ChatCacheEntity> list = new ArrayList<ChatCacheEntity>();
        list = chatEnity.queryBuilder().where(ChatCacheEntityDao.Properties.Meetingid.eq(meetingId)).list();
        return list;
    }

    public static long selectLoadListSize(Context context, String meetingId) {
        DaoSession session = getSession(context);
        ChatCacheEntityDao chatEnity = session.getChatCacheEntityDao();
        long count = chatEnity.queryBuilder().where(ChatCacheEntityDao.Properties.Meetingid.eq(meetingId)).count();
        Log.i("CRUDChat", "count" + count);
        return count;
    }

    public static List<ChatCacheEntity> setectAllList(Context context) {
        DaoSession session = getSession(context);
        ChatCacheEntityDao chatEnity = session.getChatCacheEntityDao();
        List<ChatCacheEntity> list = chatEnity.loadAll();
        return list;
    }


    public static long selectIsReadSize(Context context, String meetingId) {

        DaoSession session = getSession(context);
        ChatCacheEntityDao chatEnity = session.getChatCacheEntityDao();
        long isReadSize = chatEnity.queryBuilder().where(ChatCacheEntityDao.Properties.Meetingid.eq(meetingId)).count();
        return isReadSize;
    }

    public static ChatCacheEntity selectTopChatMessage(Context context, String meetingId) {

        /**
         *  Late modify Efficient Query
         */
        DaoSession session = getSession(context);
        ChatCacheEntityDao chatEnity = session.getChatCacheEntityDao();
        List<ChatCacheEntity> list = new ArrayList<ChatCacheEntity>();
        list = chatEnity.queryBuilder().where(ChatCacheEntityDao.Properties.Meetingid.eq(meetingId)).list();

        if (list.size() > 0) {
            return list.get(list.size() - 1);
        }
        return null;
    }


    /*******
     * Insert
     */

    public static void queryInsert(Context context, ReqSndMsgEntity resquest) {
        DaoSession session = getSession(context);
        ChatCacheEntityDao dao = session.getChatCacheEntityDao();
        if (resquest != null) {
            dao.insert(chatCacheEntityFractoty(resquest));
        }

    }


    /*******
     * delete
     */

    public static void deleteByMeetingId(Context context, String meetingId) {
        DaoSession session = getSession(context);
        ChatCacheEntityDao personDao = session.getChatCacheEntityDao();
        QueryBuilder<ChatCacheEntity> qb = personDao.queryBuilder();
        DeleteQuery<ChatCacheEntity> bd = qb.where(ChatCacheEntityDao.Properties.Meetingid.eq(meetingId)).buildDelete();
        bd.executeDeleteWithoutDetachingEntities();
    }


    public static DaoSession getSession(Context context) {
        SQLiteDatabase db = new DaoMaster.DevOpenHelper(context,
                "CHAT.db", null).getWritableDatabase();
        DaoMaster dm = new DaoMaster(db);
        DaoSession sesion = dm.newSession();
        return sesion;
    }

    private static ChatCacheEntity chatCacheEntityFractoty(ReqSndMsgEntity reqSndMsgEntity) {
        ChatCacheEntity chatCacheEntity = new ChatCacheEntity(
                reqSndMsgEntity.getRoom(),
                reqSndMsgEntity.getFrom(),
                reqSndMsgEntity.getCont(),
                reqSndMsgEntity.getNtime()+ "",
                false
        );
        return chatCacheEntity;
    }


}
