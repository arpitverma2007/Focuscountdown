package com.arpit.focuscountdown.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class AppStore(context: Context) {
    private val p = context.getSharedPreferences("focus_v1", Context.MODE_PRIVATE)

    fun goals(): MutableList<Goal> {
        val a = JSONArray(p.getString("goals", "[]"))
        val out = mutableListOf<Goal>()
        for (i in 0 until a.length()) {
            val o = a.getJSONObject(i)
            out += Goal(o.getLong("id"), o.getString("title"), o.getLong("start"),
                o.getLong("target"), o.optString("icon", "🎯"), o.optBoolean("archived", false))
        }
        return out
    }

    fun saveGoals(list: List<Goal>) {
        val a = JSONArray()
        list.forEach {
            a.put(JSONObject().apply {
                put("id", it.id); put("title", it.title); put("start", it.startMillis)
                put("target", it.targetMillis); put("icon", it.icon); put("archived", it.archived)
            })
        }
        p.edit().putString("goals", a.toString()).apply()
    }

    fun journal(): MutableList<JournalEntry> {
        val a = JSONArray(p.getString("journal", "[]"))
        val out = mutableListOf<JournalEntry>()
        for (i in 0 until a.length()) {
            val o = a.getJSONObject(i)
            out += JournalEntry(o.getLong("id"), o.getLong("date"), o.getString("text"),
                o.optDouble("hours", 0.0).toFloat(), o.optString("mood", "🙂"), o.optString("achievement", ""))
        }
        return out.sortedByDescending { it.dateMillis }.toMutableList()
    }

    fun saveJournal(list: List<JournalEntry>) {
        val a = JSONArray()
        list.forEach {
            a.put(JSONObject().apply {
                put("id", it.id); put("date", it.dateMillis); put("text", it.text)
                put("hours", it.studyHours.toDouble()); put("mood", it.mood); put("achievement", it.achievement)
            })
        }
        p.edit().putString("journal", a.toString()).apply()
    }

    fun mainGoalId(): Long = p.getLong("mainGoal", -1L)
    fun setMainGoal(id: Long) { p.edit().putLong("mainGoal", id).apply() }

    fun todayKey(): String {
        val c = Calendar.getInstance()
        return "%04d-%02d-%02d".format(c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH))
    }

    fun progress(): MutableMap<String, DailyProgress> {
        val a = JSONArray(p.getString("progress", "[]"))
        val out = mutableMapOf<String, DailyProgress>()
        for (i in 0 until a.length()) {
            val o = a.getJSONObject(i)
            val key = o.getString("key")
            out[key] = DailyProgress(key, o.optDouble("hours",0.0).toFloat(), o.optInt("tasks",0), o.optString("note",""))
        }
        return out
    }

    fun saveProgress(map: Map<String, DailyProgress>) {
        val a = JSONArray()
        map.values.forEach {
            a.put(JSONObject().apply {
                put("key", it.dateKey); put("hours", it.studyHours.toDouble())
                put("tasks", it.tasks); put("note", it.note)
            })
        }
        p.edit().putString("progress", a.toString()).apply()
    }
}
