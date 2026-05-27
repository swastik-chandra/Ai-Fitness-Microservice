"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/providers/AuthProvider";
import api from "@/lib/api";
import type { Activity } from "@/types";
import { History as HistoryIcon, Dumbbell, Clock, Flame, Search } from "lucide-react";
import { motion } from "framer-motion";
import Link from "next/link";

export default function HistoryPage() {
  const { user } = useAuth();
  const [activities, setActivities] = useState<Activity[]>([]);
  const [filtered, setFiltered] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [typeFilter, setTypeFilter] = useState<string>("ALL");

  useEffect(() => {
    async function fetchHistory() {
      if (!user?.id) return;
      try {
        const res = await api.get<Activity[]>("/api/activities", {
          headers: { "X-User-ID": user.id },
        });
        const sorted = res.data.sort(
          (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        setActivities(sorted);
        setFiltered(sorted);
      } catch {
        // handle
      } finally {
        setLoading(false);
      }
    }
    fetchHistory();
  }, [user?.id]);

  useEffect(() => {
    let result = activities;
    if (typeFilter !== "ALL") {
      result = result.filter((a) => a.type === typeFilter);
    }
    if (search) {
      result = result.filter((a) =>
        a.type.toLowerCase().includes(search.toLowerCase())
      );
    }
    setFiltered(result);
  }, [search, typeFilter, activities]);

  const uniqueTypes = [...new Set(activities.map((a) => a.type))];
  const totalCalories = filtered.reduce((s, a) => s + (a.caloriesBurned || 0), 0);
  const totalDuration = filtered.reduce((s, a) => s + (a.duration || 0), 0);

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="skeleton h-10 w-48" />
        <div className="skeleton h-16 rounded-2xl" />
        {[...Array(5)].map((_, i) => <div key={i} className="skeleton h-16 rounded-xl" />)}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-white flex items-center gap-3">
          <HistoryIcon className="w-8 h-8 text-cyan-400" />
          Activity History
        </h1>
        <p className="text-gray-400 mt-1">Your complete workout log</p>
      </div>

      {/* Stats Bar */}
      <div className="glass rounded-2xl p-4 flex items-center gap-6 flex-wrap">
        <div className="flex items-center gap-2">
          <Dumbbell className="w-4 h-4 text-blue-400" />
          <span className="text-sm text-gray-300"><b className="text-white">{filtered.length}</b> workouts</span>
        </div>
        <div className="flex items-center gap-2">
          <Flame className="w-4 h-4 text-red-400" />
          <span className="text-sm text-gray-300"><b className="text-white">{totalCalories.toLocaleString()}</b> calories</span>
        </div>
        <div className="flex items-center gap-2">
          <Clock className="w-4 h-4 text-emerald-400" />
          <span className="text-sm text-gray-300"><b className="text-white">{totalDuration}</b> minutes</span>
        </div>
      </div>

      {/* Filters */}
      <div className="flex items-center gap-3 flex-wrap">
        <div className="relative flex-1 max-w-xs">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
          <input
            type="text"
            placeholder="Search activities..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2.5 rounded-xl text-sm text-white border border-gray-700 focus:border-blue-500 outline-none transition-smooth"
            style={{ background: "rgba(255,255,255,0.03)" }}
          />
        </div>
        <div className="flex items-center gap-2 flex-wrap">
          <button
            onClick={() => setTypeFilter("ALL")}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-smooth border ${
              typeFilter === "ALL"
                ? "border-blue-500 bg-blue-500/15 text-blue-400"
                : "border-gray-700 text-gray-400 hover:border-gray-600"
            }`}
          >
            All
          </button>
          {uniqueTypes.map((type) => (
            <button
              key={type}
              onClick={() => setTypeFilter(type)}
              className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-smooth border ${
                typeFilter === type
                  ? "border-blue-500 bg-blue-500/15 text-blue-400"
                  : "border-gray-700 text-gray-400 hover:border-gray-600"
              }`}
            >
              {type.replace("_", " ")}
            </button>
          ))}
        </div>
      </div>

      {/* Table */}
      {filtered.length === 0 ? (
        <div className="glass rounded-2xl p-12 text-center">
          <HistoryIcon className="w-10 h-10 text-gray-600 mx-auto mb-3" />
          <p className="text-gray-400 text-sm">No activities match your filters</p>
        </div>
      ) : (
        <div className="glass rounded-2xl overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-800/50">
                <th className="text-left text-xs font-semibold text-gray-400 px-6 py-3">Activity</th>
                <th className="text-left text-xs font-semibold text-gray-400 px-6 py-3">Duration</th>
                <th className="text-left text-xs font-semibold text-gray-400 px-6 py-3">Calories</th>
                <th className="text-left text-xs font-semibold text-gray-400 px-6 py-3">Date</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((activity, idx) => (
                <motion.tr
                  key={activity.id}
                  className="border-b border-gray-800/30 hover:bg-white/5 transition-smooth cursor-pointer"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ delay: idx * 0.03 }}
                >
                  <td className="px-6 py-4">
                    <Link href={`/activities/${activity.id}`} className="flex items-center gap-3">
                      <div className="w-8 h-8 rounded-lg bg-blue-500/10 flex items-center justify-center">
                        <Dumbbell className="w-4 h-4 text-blue-400" />
                      </div>
                      <span className="text-sm font-medium text-white">{activity.type.replace("_", " ")}</span>
                    </Link>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-400">{activity.duration} min</td>
                  <td className="px-6 py-4 text-sm text-gray-400">{activity.caloriesBurned} cal</td>
                  <td className="px-6 py-4 text-sm text-gray-500">
                    {new Date(activity.createdAt).toLocaleDateString("en-US", {
                      month: "short", day: "numeric", year: "numeric",
                    })}
                  </td>
                </motion.tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
