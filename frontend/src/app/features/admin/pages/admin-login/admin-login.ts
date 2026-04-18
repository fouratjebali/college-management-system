import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UserRole } from '../../../../core/models/auth.model';
import { AuthService } from '../../../../core/services/auth';

@Component({
  selector: 'app-admin-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './admin-login.html',
  styleUrl: './admin-login.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminLoginComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  protected readonly feedbackMessage = signal('');
  protected readonly isSubmitting = signal(false);

  protected readonly adminLoginForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  protected submitAdminLogin(): void {
    if (this.adminLoginForm.invalid) {
      this.adminLoginForm.markAllAsTouched();
      return;
    }

    const { email, password } = this.adminLoginForm.getRawValue();
    this.isSubmitting.set(true);
    this.feedbackMessage.set('');

    this.authService.adminLogin(email, password).subscribe({
      next: (response) => {
        this.isSubmitting.set(false);

        if (response.user.role !== UserRole.ADMIN) {
          this.feedbackMessage.set('Ce compte existe, mais il ne possede pas le role administrateur.');
          return;
        }

        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/admin';
        this.router.navigateByUrl(returnUrl === '/admin/login' ? '/admin' : returnUrl);
      },
      error: (error) => {
        this.isSubmitting.set(false);
        this.feedbackMessage.set(this.resolveLoginError(error));
      },
    });
  }

  private resolveLoginError(error: { status?: number; error?: { error?: string; message?: string } }): string {
    if (error.status === 0) {
      return 'Service momentanement indisponible. Veuillez reessayer dans quelques instants.';
    }

    return error.error?.error || error.error?.message || 'Connexion admin refusee.';
  }
}
