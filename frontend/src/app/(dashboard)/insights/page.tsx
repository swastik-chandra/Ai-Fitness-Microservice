"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/providers/AuthProvider";
import api from "@/lib/api";
import type { Recommendation } from "@/types";
import { Brain, TrendingUp, Lightbulb, Shield, Zap } from "lucide-react";
import { motion } from "framer-motion";

export default function InsightsPage() {
  const { user } = useAuth();
  const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchInsights() {
      if (!user?.id) return;
      try {
        const res = await api.get<Recommendation[]>(`/api/recommendations/user/${user.id}`);
        setRecommendations(res.data);
      } catch {
        // handle
      } finally {
        setLoading(false);
      }
    }
    fetchInsights();
  }, [user?.id]);

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="skeleton h-10 w-48" />
        {[...Array(3)].map((_, i) => <div key={i} className="skeleton h-64 rounded-2xl" />)}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-white flex items-center gap-3">
          <Zap className="w-8 h-8 text-purple-400" />
          AI Insights
        </h1>
        <p className="text-gray-400 mt-1">Personalized recommendations powered by Google Gemini AI</p>
      </div>

      {recommendations.length === 0 ? (
        <div className="glass rounded-2xl p-16 text-center">
          <Brain className="w-14 h-14 text-gray-600 mx-auto mb-4" />
          <h3 className="text-xl font-semibold text-white mb-2">No Insights Yet</h3>
          <p className="text-gray-400 text-sm max-w-md mx-auto">
            Log your first workout and our AI will analyze your activity to provide personalized fitness recommendations.
          </p>
        </div>
      ) : (
        <div className="space-y-6">
          {recommendations.map((rec, idx) => (
            <motion.div
              key={rec.id}
              className="glass rounded-2xl p-6 gradient-border"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: idx * 0.1 }}
            >
              <div className="flex items-center gap-3 mb-5">
                <div className="w-10 h-10 rounded-xl bg-purple-500/10 flex items-center justify-center">
                  <Brain className="w-5 h-5 text-purple-400" />
                </div>
                <div>
                  <h3 className="text-base font-semibold text-white">{rec.activityType} Analysis</h3>
                  <p className="text-xs text-gray-500">
                    {new Date(rec.createdAt).toLocaleDateString("en-US", {
                      month: "long", day: "numeric", year: "numeric",
                    })}
                  </p>
                </div>
              </div>

              <div className="p-4 rounded-xl bg-purple-500/5 border border-purple-500/10 mb-5">
                <p className="text-sm text-gray-300 leading-relaxed whitespace-pre-line">{rec.recommendation}</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                {/* Improvements */}
                <div className="p-4 rounded-xl bg-white/5">
                  <h4 className="text-xs font-semibold text-emerald-400 flex items-center gap-1.5 mb-3">
                    <TrendingUp className="w-3.5 h-3.5" />
                    Improvements
                  </h4>
                  <ul className="space-y-2">
                    {rec.improvements?.map((imp, i) => (
                      <li key={i} className="text-xs text-gray-400 leading-relaxed">{imp}</li>
                    ))}
                  </ul>
                </div>

                {/* Suggestions */}
                <div className="p-4 rounded-xl bg-white/5">
                  <h4 className="text-xs font-semibold text-blue-400 flex items-center gap-1.5 mb-3">
                    <Lightbulb className="w-3.5 h-3.5" />
                    Suggestions
                  </h4>
                  <ul className="space-y-2">
                    {rec.suggestions?.map((sug, i) => (
                      <li key={i} className="text-xs text-gray-400 leading-relaxed">{sug}</li>
                    ))}
                  </ul>
                </div>

                {/* Safety */}
                <div className="p-4 rounded-xl bg-white/5">
                  <h4 className="text-xs font-semibold text-amber-400 flex items-center gap-1.5 mb-3">
                    <Shield className="w-3.5 h-3.5" />
                    Safety
                  </h4>
                  <ul className="space-y-2">
                    {rec.safety?.map((s, i) => (
                      <li key={i} className="text-xs text-gray-400 leading-relaxed">{s}</li>
                    ))}
                  </ul>
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
}
