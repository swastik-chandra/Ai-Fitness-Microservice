"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/providers/AuthProvider";
import api from "@/lib/api";
import type { Activity, Recommendation } from "@/types";
import { Dumbbell, Flame, Clock, Brain, TrendingUp, Zap } from "lucide-react";
import { motion } from "framer-motion";
import Link from "next/link";

export default function DashboardPage() {
  const { user } = useAuth();
  const [activities, setActivities] = useState<Activity[]>([]);
  const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchData() {
      try {
        const [actRes, recRes] = await Promise.allSettled([
          api.get<Activity[]>("/api/activities", { headers: { "X-User-ID": user?.id || "" } }),
          api.get<Recommendation[]>(`/api/recommendations/user/${user?.id}`),
        ]);
        if (actRes.status === "fulfilled") setActivities(actRes.value.data);
        if (recRes.status === "fulfilled") setRecommendations(recRes.value.data);
      } catch {
        // Silently fail — dashboard shows empty state
      } finally {
        setLoading(false);
      }
    }
    if (user?.id) fetchData();
  }, [user?.id]);

  const totalCalories = activities.reduce((sum, a) => sum + (a.caloriesBurned || 0), 0);
  const totalDuration = activities.reduce((sum, a) => sum + (a.duration || 0), 0);
  const totalWorkouts = activities.length;
  const totalInsights = recommendations.length;

  const stats = [
    { label: "Total Workouts", value: totalWorkouts, icon: Dumbbell, color: "#3b82f6", bg: "bg-blue-500/10" },
    { label: "Calories Burned", value: totalCalories.toLocaleString(), icon: Flame, color: "#ef4444", bg: "bg-red-500/10" },
    { label: "Minutes Active", value: totalDuration, icon: Clock, color: "#10b981", bg: "bg-emerald-500/10" },
    { label: "AI Insights", value: totalInsights, icon: Brain, color: "#8b5cf6", bg: "bg-purple-500/10" },
  ];

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="skeleton h-10 w-64" />
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
          {[...Array(4)].map((_, i) => <div key={i} className="skeleton h-32 rounded-2xl" />)}
        </div>
        <div className="skeleton h-64 rounded-2xl" />
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.5 }}>
        <h1 className="text-3xl font-bold text-white">
          Welcome back, <span className="gradient-text">{user?.firstName || "Athlete"}</span> 👋
        </h1>
        <p className="text-gray-400 mt-1">Here&apos;s your fitness overview</p>
      </motion.div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
        {stats.map((stat, idx) => (
          <motion.div
            key={stat.label}
            className="glass rounded-2xl p-5 transition-smooth hover:scale-[1.02]"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: idx * 0.1 }}
          >
            <div className="flex items-center justify-between mb-3">
              <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${stat.bg}`}>
                <stat.icon className="w-5 h-5" style={{ color: stat.color }} />
              </div>
              <TrendingUp className="w-4 h-4 text-emerald-400" />
            </div>
            <p className="text-2xl font-bold text-white">{stat.value}</p>
            <p className="text-sm text-gray-400 mt-1">{stat.label}</p>
          </motion.div>
        ))}
      </div>

      {/* Recent Activities + AI Insights */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Activities */}
        <motion.div
          className="glass rounded-2xl p-6"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.4 }}
        >
          <div className="flex items-center justify-between mb-5">
            <h2 className="text-lg font-semibold text-white">Recent Activities</h2>
            <Link href="/activities" className="text-sm text-blue-400 hover:text-blue-300 transition-smooth">
              View All →
            </Link>
          </div>
          {activities.length === 0 ? (
            <div className="text-center py-10">
              <Dumbbell className="w-10 h-10 text-gray-600 mx-auto mb-3" />
              <p className="text-gray-400 text-sm">No activities yet</p>
              <Link href="/activities" className="text-blue-400 text-sm hover:text-blue-300 mt-1 inline-block">
                Log your first workout →
              </Link>
            </div>
          ) : (
            <div className="space-y-3">
              {activities.slice(0, 5).map((activity) => (
                <Link
                  key={activity.id}
                  href={`/activities/${activity.id}`}
                  className="flex items-center gap-4 p-3 rounded-xl hover:bg-white/5 transition-smooth group"
                >
                  <div className="w-10 h-10 rounded-xl bg-blue-500/10 flex items-center justify-center">
                    <Dumbbell className="w-5 h-5 text-blue-400" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-white">{activity.type.replace("_", " ")}</p>
                    <p className="text-xs text-gray-500">{activity.duration} min • {activity.caloriesBurned} cal</p>
                  </div>
                  <span className="text-xs text-gray-500">
                    {new Date(activity.createdAt).toLocaleDateString()}
                  </span>
                </Link>
              ))}
            </div>
          )}
        </motion.div>

        {/* AI Insights Preview */}
        <motion.div
          className="glass rounded-2xl p-6"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.5 }}
        >
          <div className="flex items-center justify-between mb-5">
            <h2 className="text-lg font-semibold text-white flex items-center gap-2">
              <Zap className="w-4 h-4 text-purple-400" />
              AI Insights
            </h2>
            <Link href="/insights" className="text-sm text-purple-400 hover:text-purple-300 transition-smooth">
              View All →
            </Link>
          </div>
          {recommendations.length === 0 ? (
            <div className="text-center py-10">
              <Brain className="w-10 h-10 text-gray-600 mx-auto mb-3" />
              <p className="text-gray-400 text-sm">No AI insights yet</p>
              <p className="text-gray-500 text-xs mt-1">Log a workout to get AI recommendations</p>
            </div>
          ) : (
            <div className="space-y-3">
              {recommendations.slice(0, 4).map((rec) => (
                <div
                  key={rec.id}
                  className="p-3 rounded-xl border border-purple-500/10 bg-purple-500/5"
                >
                  <div className="flex items-center gap-2 mb-1">
                    <Brain className="w-3.5 h-3.5 text-purple-400" />
                    <span className="text-xs font-medium text-purple-400">{rec.activityType}</span>
                  </div>
                  <p className="text-sm text-gray-300 line-clamp-2">{rec.recommendation}</p>
                </div>
              ))}
            </div>
          )}
        </motion.div>
      </div>
    </div>
  );
}
