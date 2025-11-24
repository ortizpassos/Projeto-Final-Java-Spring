export interface User {
  id: string;
  email: string;
  nome?: string; // nome completo recebido do backend
  firstName?: string;
  lastName?: string;
  phone?: string;
  document?: string;
  createdAt?: Date;
  updatedAt?: Date;
  isEmailVerified?: boolean; // mapeado de emailVerificado do backend
  isActive?: boolean;
}

export interface UserRegistration {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
  document?: string;
}

export interface UserLogin {
  email: string;
  password: string;
}

export interface AuthResponse {
  success: boolean;
  data?: {
    user: User;
    token: string;
    refreshToken?: string; // manter opcional para compatibilidade
    expiresIn: number;
  };
  error?: {
    message: string;
    needsVerification?: boolean;
    email?: string;
    code?: string;
  };
}
