"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import api from "@/lib/api";
import type { Activity, Recommendation } from "@/types";
import { Dumbbell, Clock, Flame, Brain, Shield, TrendingUp, Lightbulb, ArrowLeft } from "lucide-react";
import { motion } from "framer-motion";
import Link from "next/link";

export default function ActivityDetailPage() {
  const params = useParams();
  const activityId = params.id as string;
  const [activity, setActivity] = useState<Activity | null>(null);
  const [recommendation, setRecommendation] = useState<Recommendation | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchData() {
      try {
        const [actRes, recRes] = await Promise.allSettled([
          api.get<Activity>(`/api/activities/${activityId}`),
          api.get<Recommendation>(`/api/recommendations/activity/${activityId}`),
        ]);
        if (actRes.status === "fulfilled") setActivity(actRes.value.data);
        if (recRes.status === "fulfilled") setRecommendation(recRes.value.data);
      } catch {
        // handle error
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, [activityId]);

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="skeleton h-8 w-32" />
        <div className="skeleton h-48 rounded-2xl" />
        <div className="skeleton h-64 rounded-2xl" />
      </div>
    );
  }

  if (!activity) {
    return (
      <div className="text-center py-20">
        <p className="text-gray-400">Activity not found</p>
        <Link href="/activities" className="text-blue-400 text-sm mt-2 inline-block">← Back to activities</Link>
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Back */}
      <Link href="/activities" className="inline-flex items-center gap-2 text-gray-400 hover:text-white text-sm transition-smooth">
        <ArrowLeft className="w-4 h-4" />
        Back to Activities
      </Link>

      {/* Activity Card */}
      <motion.div
        className="glass rounded-2xl p-8"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="flex items-center gap-4 mb-6">
          <div className="w-14 h-14 rounded-2xl bg-blue-500/10 flex items-center justify-center">
            <Dumbbell className="w-7 h-7 text-blue-400" />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-white">{activity.type.replace("_", " ")}</h1>
            <p className="text-gray-400 text-sm">
              {new Date(activity.createdAt).toLocaleDateString("en-US", {
                weekday: "long", month: "long", day: "numeric", year: "numeric",
              })}
            </p>
          </div>
        </div>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
          <div className="p-4 rounded-xl bg-white/5">
            <div className="flex items-center gap-2 text-gray-400 mb-1">
              <Clock className="w-4 h-4" />
              <span className="text-xs">Duration</span>
            </div>
            <p className="text-xl font-bold text-white">{activity.duration} min</p>
          </div>
          <div className="p-4 rounded-xl bg-white/5">
            <div className="flex items-center gap-2 text-gray-400 mb-1">
              <Flame className="w-4 h-4" />
              <span className="text-xs">Calories</span>
            </div>
            <p className="text-xl font-bold text-white">{activity.caloriesBurned}</p>
          </div>
          <div className="p-4 rounded-xl bg-white/5">
            <div className="flex items-center gap-2 text-gray-400 mb-1">
              <TrendingUp className="w-4 h-4" />
              <span className="text-xs">Type</span>
            </div>
            <p className="text-xl font-bold text-white">{activity.type}</p>
          </div>
        </div>
      </motion.div>

      {/* AI Recommendation */}
      {recommendation ? (
        <motion.div
          className="glass rounded-2xl p-8 gradient-border"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
        >
          <div className="flex items-center gap-2 mb-6">
            <Brain className="w-5 h-5 text-purple-400" />
            <h2 className="text-xl font-bold text-white">AI Analysis</h2>
          </div>

          {/* Overall Recommendation */}
          <div className="mb-6 p-4 rounded-xl bg-purple-500/5 border border-purple-500/10">
            <p className="text-sm text-gray-300 leading-relaxed whitespace-pre-line">{recommendation.recommendation}</p>
          </div>

          {/* Improvements */}
          {recommendation.improvements?.length > 0 && (
            <div className="mb-5">
              <h3 className="text-sm font-semibold text-emerald-400 flex items-center gap-2 mb-3">
                <TrendingUp className="w-4 h-4" />
                Improvements
              </h3>
              <ul className="space-y-2">
                {recommendation.improvements.map((imp, i) => (
                  <li key={i} className="text-sm text-gray-400 pl-4 border-l-2 border-emerald-500/30">{imp}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Suggestions */}
          {recommendation.suggestions?.length > 0 && (
            <div className="mb-5">
              <h3 className="text-sm font-semibold text-blue-400 flex items-center gap-2 mb-3">
                <Lightbulb className="w-4 h-4" />
                Suggestions
              </h3>
              <ul className="space-y-2">
                {recommendation.suggestions.map((sug, i) => (
                  <li key={i} className="text-sm text-gray-400 pl-4 border-l-2 border-blue-500/30">{sug}</li>
                ))}
              </ul>
            </div>
          )}

          {/* Safety */}
          {recommendation.safety?.length > 0 && (
            <div>
              <h3 className="text-sm font-semibold text-amber-400 flex items-center gap-2 mb-3">
                <Shield className="w-4 h-4" />
                Safety Guidelines
              </h3>
              <ul className="space-y-2">
                {recommendation.safety.map((s, i) => (
                  <li key={i} className="text-sm text-gray-400 pl-4 border-l-2 border-amber-500/30">{s}</li>
                ))}
              </ul>
            </div>
          )}
        </motion.div>
      ) : (
        <div className="glass rounded-2xl p-8 text-center">
          <Brain className="w-10 h-10 text-gray-600 mx-auto mb-3" />
          <p className="text-gray-400 text-sm">AI recommendation is being generated...</p>
          <p className="text-gray-500 text-xs mt-1">This may take a moment as the AI analyzes your activity</p>
        </div>
      )}
    </div>
  );
}
