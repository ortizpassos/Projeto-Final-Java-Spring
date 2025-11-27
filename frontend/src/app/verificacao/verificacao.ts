import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-verificacao-email',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './verificacao.html',
  styleUrls: ['./verificacao.css']
})
export class VerificacaoEmailComponent implements OnInit {
  email = '';
  codigo = '';
  minutosRestantes = 30;
  segundosRestantes = 0;
  timerInterval: any;
  carregando = false;
  erro = '';
  sucesso = '';
  reenviando = false;
  bloqueioReenvioSegundos = 10;
  restanteBloqueio = 0;

  constructor(private route: ActivatedRoute, private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.email = this.route.snapshot.queryParamMap.get('email') || '';
    this.iniciarTimer();
  }

  iniciarTimer() {
    this.timerInterval = setInterval(() => {
      if (this.segundosRestantes === 0) {
        if (this.minutosRestantes === 0) {
          clearInterval(this.timerInterval);
        } else {
          this.minutosRestantes--;
          this.segundosRestantes = 59;
        }
      } else {
        this.segundosRestantes--;
      }
    }, 1000);
  }

  formatarTempo(): string {
    const mm = this.minutosRestantes.toString().padStart(2, '0');
    const ss = this.segundosRestantes.toString().padStart(2, '0');
    return `${mm}:${ss}`;
  }

  onSubmit() {
    console.log('Verificacao onSubmit', this.email, this.codigo);
    if (!this.email || !this.codigo) {
      this.erro = 'Informe o código.';
      return;
    }
    this.carregando = true;
    this.erro = '';
    this.sucesso = '';
    this.auth.verifyEmail(this.email, this.codigo).subscribe({
      next: (res) => {
        console.log('Verificacao success', res);
        this.sucesso = 'E-mail verificado! Redirecionando...';
        this.carregando = false;
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1500);
      },
      error: (err: any) => {
        console.error('Verificacao error', err);
        this.erro = err.error?.message || 'Falha ao verificar código';
        this.carregando = false;
      }
    });
  }

  podeReenviar(): boolean {
    return !this.reenviando && this.restanteBloqueio === 0;
  }

  reenviarCodigo() {
    if (!this.podeReenviar()) return;
    this.reenviando = true;
    this.auth.resendCode(this.email).subscribe({
      next: () => {
        this.sucesso = 'Novo código enviado para o e-mail.';
        this.erro = '';
        this.minutosRestantes = 30;
        this.segundosRestantes = 0;
        if (this.timerInterval) clearInterval(this.timerInterval);
        this.iniciarTimer();
        this.restanteBloqueio = this.bloqueioReenvioSegundos;
        const bloqueioTimer = setInterval(() => {
          if (this.restanteBloqueio > 0) {
            this.restanteBloqueio--;
          } else {
            clearInterval(bloqueioTimer);
            this.reenviando = false;
          }
        }, 1000);
      },
      error: (err: any) => {
        this.erro = err.error?.message || 'Falha ao reenviar código';
        this.reenviando = false;
      }
    });
  }

  voltarLogin() {
    this.router.navigate(['/login']);
  }
}
