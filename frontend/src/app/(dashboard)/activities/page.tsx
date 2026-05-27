"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/providers/AuthProvider";
import api from "@/lib/api";
import type { Activity, ActivityType, ActivityRequest } from "@/types";
import { Dumbbell, Plus, X, Loader2, Clock, Flame } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import Link from "next/link";

const ACTIVITY_TYPES: ActivityType[] = [
  "RUNNING", "WALKING", "CYCLING", "SWIMMING", "WEIGHT_TRAINING",
  "YOGA", "HIIT", "CARDIO", "STRETCHING", "OTHER",
];

const typeColors: Record<string, string> = {
  RUNNING: "#3b82f6", WALKING: "#10b981", CYCLING: "#f59e0b",
  SWIMMING: "#06b6d4", WEIGHT_TRAINING: "#ef4444", YOGA: "#8b5cf6",
  HIIT: "#f97316", CARDIO: "#ec4899", STRETCHING: "#14b8a6", OTHER: "#6b7280",
};

export default function ActivitiesPage() {
  const { user } = useAuth();
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [form, setForm] = useState<Partial<ActivityRequest>>({
    type: "RUNNING",
    duration: 30,
    caloriesBurned: 200,
  });

  useEffect(() => {
    fetchActivities();
  }, [user?.id]);

  async function fetchActivities() {
    if (!user?.id) return;
    try {
      const res = await api.get<Activity[]>("/api/activities", {
        headers: { "X-User-ID": user.id },
      });
      setActivities(res.data);
    } catch {
      // handle error
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!user?.id) return;
    setSubmitting(true);
    try {
      await api.post("/api/activities", {
        ...form,
        userId: user.id,
        startTime: new Date().toISOString(),
      });
      setShowForm(false);
      setForm({ type: "RUNNING", duration: 30, caloriesBurned: 200 });
      fetchActivities();
    } catch {
      // handle error
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="skeleton h-10 w-48" />
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {[...Array(6)].map((_, i) => <div key={i} className="skeleton h-40 rounded-2xl" />)}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white">Activities</h1>
          <p className="text-gray-400 mt-1">Track and manage your workouts</p>
        </div>
        <button
          onClick={() => setShowForm(true)}
          className="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-semibold text-white transition-smooth hover:scale-105"
          style={{ background: "linear-gradient(135deg, #3b82f6, #2563eb)" }}
        >
          <Plus className="w-4 h-4" />
          Log Workout
        </button>
      </div>

      {/* New Activity Modal */}
      <AnimatePresence>
        {showForm && (
          <motion.div
            className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
          >
            <motion.div
              className="glass rounded-2xl p-8 w-full max-w-md mx-4"
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
            >
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-xl font-bold text-white">Log Workout</h3>
                <button onClick={() => setShowForm(false)} className="text-gray-400 hover:text-white">
                  <X className="w-5 h-5" />
                </button>
              </div>
              <form onSubmit={handleSubmit} className="space-y-5">
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-2">Activity Type</label>
                  <div className="grid grid-cols-2 gap-2">
                    {ACTIVITY_TYPES.map((type) => (
                      <button
                        key={type}
                        type="button"
                        onClick={() => setForm({ ...form, type })}
                        className={`px-3 py-2 rounded-lg text-xs font-medium transition-smooth border ${
                          form.type === type
                            ? "border-blue-500 bg-blue-500/15 text-blue-400"
                            : "border-gray-700 text-gray-400 hover:border-gray-600"
                        }`}
                      >
                        {type.replace("_", " ")}
                      </button>
                    ))}
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">Duration (min)</label>
                    <input
                      type="number"
                      value={form.duration || ""}
                      onChange={(e) => setForm({ ...form, duration: parseInt(e.target.value) })}
                      min={1}
                      required
                      className="w-full px-4 py-3 rounded-xl text-sm text-white border border-gray-700 focus:border-blue-500 outline-none transition-smooth"
                      style={{ background: "rgba(255,255,255,0.03)" }}
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">Calories</label>
                    <input
                      type="number"
                      value={form.caloriesBurned || ""}
                      onChange={(e) => setForm({ ...form, caloriesBurned: parseInt(e.target.value) })}
                      min={0}
                      required
                      className="w-full px-4 py-3 rounded-xl text-sm text-white border border-gray-700 focus:border-blue-500 outline-none transition-smooth"
                      style={{ background: "rgba(255,255,255,0.03)" }}
                    />
                  </div>
                </div>
                <button
                  type="submit"
                  disabled={submitting}
                  className="w-full py-3 rounded-xl text-sm font-semibold text-white transition-smooth hover:scale-[1.02] disabled:opacity-50"
                  style={{ background: "linear-gradient(135deg, #3b82f6, #2563eb)" }}
                >
                  {submitting ? <Loader2 className="w-4 h-4 animate-spin mx-auto" /> : "Save Activity"}
                </button>
              </form>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Activities Grid */}
      {activities.length === 0 ? (
        <div className="glass rounded-2xl p-16 text-center">
          <Dumbbell className="w-12 h-12 text-gray-600 mx-auto mb-4" />
          <h3 className="text-lg font-semibold text-white mb-2">No Activities Yet</h3>
          <p className="text-gray-400 text-sm mb-6">Start tracking your workouts to see them here</p>
          <button
            onClick={() => setShowForm(true)}
            className="px-6 py-2.5 rounded-xl text-sm font-semibold text-white"
            style={{ background: "linear-gradient(135deg, #3b82f6, #2563eb)" }}
          >
            Log Your First Workout
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {activities.map((activity, idx) => (
            <motion.div
              key={activity.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: idx * 0.05 }}
            >
              <Link
                href={`/activities/${activity.id}`}
                className="block glass rounded-2xl p-5 transition-smooth hover:scale-[1.02] group"
              >
                <div className="flex items-center gap-3 mb-4">
                  <div
                    className="w-10 h-10 rounded-xl flex items-center justify-center"
                    style={{ background: `${typeColors[activity.type] || "#6b7280"}15` }}
                  >
                    <Dumbbell className="w-5 h-5" style={{ color: typeColors[activity.type] || "#6b7280" }} />
                  </div>
                  <div>
                    <h3 className="text-sm font-semibold text-white">{activity.type.replace("_", " ")}</h3>
                    <p className="text-xs text-gray-500">
                      {new Date(activity.createdAt).toLocaleDateString("en-US", {
                        month: "short", day: "numeric", year: "numeric",
                      })}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-4">
                  <div className="flex items-center gap-1.5 text-gray-400">
                    <Clock className="w-3.5 h-3.5" />
                    <span className="text-xs">{activity.duration} min</span>
                  </div>
                  <div className="flex items-center gap-1.5 text-gray-400">
                    <Flame className="w-3.5 h-3.5" />
                    <span className="text-xs">{activity.caloriesBurned} cal</span>
                  </div>
                </div>
              </Link>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
}
