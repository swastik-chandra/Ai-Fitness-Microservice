// TypeScript interfaces for the AI Fitness Platform

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
  email: string;
  role: string;
  expiresIn: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface Activity {
  id: string;
  userId: string;
  type: ActivityType;
  duration: number;
  caloriesBurned: number;
  startTime: string;
  additionalMatrics: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
}

export type ActivityType =
  | "RUNNING"
  | "WALKING"
  | "CYCLING"
  | "SWIMMING"
  | "WEIGHT_TRAINING"
  | "YOGA"
  | "HIIT"
  | "CARDIO"
  | "STRETCHING"
  | "OTHER";

export interface ActivityRequest {
  userId: string;
  type: ActivityType;
  duration: number;
  caloriesBurned: number;
  startTime: string;
  additionalMatrics?: Record<string, unknown>;
}

export interface Recommendation {
  id: string;
  activityId: string;
  userId: string;
  activityType: string;
  recommendation: string;
  improvements: string[];
  suggestions: string[];
  safety: string[];
  createdAt: string;
}
