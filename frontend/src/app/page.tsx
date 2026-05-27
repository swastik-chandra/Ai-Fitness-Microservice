"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/providers/AuthProvider";
import { Activity, Brain, Dumbbell, TrendingUp, Zap, Shield } from "lucide-react";
import Link from "next/link";
import { motion } from "framer-motion";

export default function LandingPage() {
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      router.push("/dashboard");
    }
  }, [isAuthenticated, isLoading, router]);

  if (isLoading) return null;

  const features = [
    {
      icon: <Dumbbell className="w-6 h-6" />,
      title: "Activity Tracking",
      description: "Log your workouts with detailed metrics and track progress over time.",
      color: "text-blue-400",
      glow: "glow-blue",
    },
    {
      icon: <Brain className="w-6 h-6" />,
      title: "AI Recommendations",
      description: "Get personalized workout suggestions powered by Google Gemini AI.",
      color: "text-purple-400",
      glow: "glow-purple",
    },
    {
      icon: <TrendingUp className="w-6 h-6" />,
      title: "Progress Analytics",
      description: "Visualize your fitness journey with beautiful charts and insights.",
      color: "text-emerald-400",
      glow: "glow-green",
    },
    {
      icon: <Zap className="w-6 h-6" />,
      title: "Real-time Processing",
      description: "Event-driven architecture ensures instant AI analysis of your activities.",
      color: "text-yellow-400",
      glow: "",
    },
    {
      icon: <Shield className="w-6 h-6" />,
      title: "Secure Platform",
      description: "JWT authentication and role-based access protect your fitness data.",
      color: "text-rose-400",
      glow: "",
    },
    {
      icon: <Activity className="w-6 h-6" />,
      title: "10+ Activity Types",
      description: "From running to yoga, track any exercise with specialized metrics.",
      color: "text-cyan-400",
      glow: "",
    },
  ];

  return (
    <div className="min-h-screen relative overflow-hidden">
      {/* Animated background gradient */}
      <div className="fixed inset-0 z-0">
        <div
          className="absolute top-0 left-1/4 w-96 h-96 rounded-full blur-3xl opacity-20"
          style={{ background: "radial-gradient(circle, #3b82f6, transparent)" }}
        />
        <div
          className="absolute bottom-1/4 right-1/4 w-96 h-96 rounded-full blur-3xl opacity-15"
          style={{ background: "radial-gradient(circle, #8b5cf6, transparent)" }}
        />
        <div
          className="absolute top-1/2 left-1/2 w-72 h-72 rounded-full blur-3xl opacity-10"
          style={{ background: "radial-gradient(circle, #10b981, transparent)" }}
        />
      </div>

      {/* Navbar */}
      <nav className="relative z-10 flex items-center justify-between px-8 py-5 glass">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg flex items-center justify-center" style={{ background: "linear-gradient(135deg, #3b82f6, #8b5cf6)" }}>
            <Dumbbell className="w-5 h-5 text-white" />
          </div>
          <span className="text-xl font-bold gradient-text">FitAI</span>
        </div>
        <div className="flex items-center gap-4">
          <Link
            href="/login"
            className="px-5 py-2 text-sm font-medium text-gray-300 hover:text-white transition-smooth"
          >
            Sign In
          </Link>
          <Link
            href="/register"
            className="px-5 py-2.5 text-sm font-semibold rounded-lg text-white transition-smooth"
            style={{ background: "linear-gradient(135deg, #3b82f6, #8b5cf6)" }}
          >
            Get Started
          </Link>
        </div>
      </nav>

      {/* Hero */}
      <main className="relative z-10 flex flex-col items-center justify-center text-center px-6 pt-24 pb-16">
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8 }}
        >
          <span className="inline-block px-4 py-1.5 mb-6 text-xs font-semibold tracking-wider uppercase rounded-full border border-blue-500/30 text-blue-400 bg-blue-500/10">
            Powered by Google Gemini AI
          </span>
          <h1 className="text-5xl md:text-7xl font-extrabold leading-tight mb-6">
            Your Fitness,{" "}
            <span className="gradient-text">Supercharged</span>
            <br />
            with AI
          </h1>
          <p className="max-w-2xl mx-auto text-lg text-gray-400 mb-10 leading-relaxed">
            Track workouts, get AI-powered recommendations, and unlock insights that transform
            your fitness journey. Built on a scalable microservices architecture.
          </p>
          <div className="flex items-center justify-center gap-4">
            <Link
              href="/register"
              className="px-8 py-3.5 text-base font-semibold rounded-xl text-white glow-blue transition-smooth hover:scale-105"
              style={{ background: "linear-gradient(135deg, #3b82f6, #2563eb)" }}
            >
              Start Free
            </Link>
            <Link
              href="/login"
              className="px-8 py-3.5 text-base font-semibold rounded-xl text-gray-300 border border-gray-700 hover:border-gray-500 hover:text-white transition-smooth"
            >
              Sign In →
            </Link>
          </div>
        </motion.div>

        {/* Features */}
        <motion.div
          className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mt-28 max-w-6xl w-full"
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.3 }}
        >
          {features.map((feature, idx) => (
            <div
              key={idx}
              className="group p-6 rounded-2xl glass transition-smooth hover:scale-[1.02] cursor-default"
              style={{ animationDelay: `${idx * 100}ms` }}
            >
              <div className={`mb-4 ${feature.color}`}>{feature.icon}</div>
              <h3 className="text-lg font-semibold text-white mb-2">{feature.title}</h3>
              <p className="text-sm text-gray-400 leading-relaxed">{feature.description}</p>
            </div>
          ))}
        </motion.div>

        {/* Architecture badge */}
        <motion.div
          className="mt-20 glass rounded-2xl p-8 max-w-3xl w-full"
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.6 }}
        >
          <h2 className="text-2xl font-bold mb-4 gradient-text">Built for Scale</h2>
          <div className="flex flex-wrap justify-center gap-3">
            {[
              "Spring Boot 4",
              "Next.js 16",
              "Eureka Discovery",
              "Apache Kafka",
              "PostgreSQL",
              "MongoDB",
              "Redis",
              "Google Gemini AI",
              "JWT Auth",
              "Docker",
              "Kubernetes",
            ].map((tech) => (
              <span
                key={tech}
                className="px-4 py-1.5 text-xs font-medium rounded-full border border-gray-700 text-gray-300 bg-white/5"
              >
                {tech}
              </span>
            ))}
          </div>
        </motion.div>
      </main>

      {/* Footer */}
      <footer className="relative z-10 text-center py-8 text-gray-500 text-sm border-t border-gray-800/50">
        <p>© 2026 FitAI — AI Fitness Microservice Platform. Built with ❤️</p>
      </footer>
    </div>
  );
}
