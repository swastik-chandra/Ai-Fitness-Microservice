"use client";

import { useAuth } from "@/providers/AuthProvider";
import { User, Mail, Calendar, Shield } from "lucide-react";
import { motion } from "framer-motion";

export default function ProfilePage() {
  const { user } = useAuth();

  if (!user) return null;

  const profileFields = [
    { label: "First Name", value: user.firstName, icon: User },
    { label: "Last Name", value: user.lastName, icon: User },
    { label: "Email", value: user.email, icon: Mail },
    { label: "Role", value: user.role, icon: Shield },
    {
      label: "Member Since",
      value: new Date(user.createdAt).toLocaleDateString("en-US", {
        month: "long",
        day: "numeric",
        year: "numeric",
      }),
      icon: Calendar,
    },
  ];

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h1 className="text-3xl font-bold text-white">Profile</h1>
        <p className="text-gray-400 mt-1">Your account information</p>
      </div>

      <motion.div
        className="glass rounded-2xl p-8"
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        {/* Avatar */}
        <div className="flex items-center gap-5 mb-8 pb-6 border-b border-gray-800/50">
          <div
            className="w-20 h-20 rounded-2xl flex items-center justify-center text-3xl font-bold text-white"
            style={{ background: "linear-gradient(135deg, #3b82f6, #8b5cf6)" }}
          >
            {user.firstName?.[0]?.toUpperCase() || "U"}
          </div>
          <div>
            <h2 className="text-xl font-bold text-white">{user.firstName} {user.lastName}</h2>
            <p className="text-gray-400 text-sm">{user.email}</p>
            <span className="inline-block mt-2 px-3 py-1 text-xs font-medium rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/20">
              {user.role}
            </span>
          </div>
        </div>

        {/* Fields */}
        <div className="space-y-4">
          {profileFields.map((field) => (
            <div key={field.label} className="flex items-center gap-4 p-4 rounded-xl bg-white/5">
              <div className="w-10 h-10 rounded-xl bg-white/5 flex items-center justify-center">
                <field.icon className="w-4 h-4 text-gray-400" />
              </div>
              <div>
                <p className="text-xs text-gray-500">{field.label}</p>
                <p className="text-sm font-medium text-white">{field.value || "—"}</p>
              </div>
            </div>
          ))}
        </div>
      </motion.div>
    </div>
  );
}
