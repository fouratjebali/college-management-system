import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';

type AuthMode = 'login' | 'signup';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value as string | undefined;
  const confirmPassword = control.get('confirmPassword')?.value as string | undefined;

  if (!password || !confirmPassword) {
    return null;
  }

  return password === confirmPassword ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-auth-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auth-page.html',
  styleUrl: './auth-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthPageComponent {
  private readonly formBuilder = inject(FormBuilder);
  protected readonly mode = signal<AuthMode>('login');
  protected readonly feedbackMessage = signal('');
  protected readonly roles = [
    { value: 'STUDENT', label: 'Student' },
    { value: 'PROFESSOR', label: 'Professor' },
    { value: 'ADMIN', label: 'Administrator' },
  ] as const;

  protected readonly loginForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    rememberMe: [true],
  });

  protected readonly signupForm = this.formBuilder.nonNullable.group(
    {
      fullName: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      role: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordMatchValidator }
  );

  protected readonly title = computed(() =>
    this.mode() === 'login' ? 'Hello, Welcome Back' : 'Create Your Account'
  );

  protected readonly subtitle = computed(() =>
    this.mode() === 'login'
      ? 'Access your college space to manage courses, grades and academic follow-up.'
      : 'Set up your profile to start using the college management platform.'
  );

  protected readonly visualTitle = computed(() =>
    this.mode() === 'login' ? 'Secure access for your campus ecosystem.' : 'Join the platform in minutes.'
  );

  protected readonly visualText = computed(() =>
    this.mode() === 'login'
      ? 'Students, professors and administrators enter through one consistent, modern gateway.'
      : 'Create a profile, choose a role and prepare the space you will use across the academic year.'
  );

  protected setMode(mode: AuthMode): void {
    this.mode.set(mode);
    this.feedbackMessage.set('');
  }

  protected submitLogin(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    const { email } = this.loginForm.getRawValue();
    this.feedbackMessage.set(
      `Login interface ready for ${email}. The next step will be connecting this form to your backend authentication API.`
    );
  }

  protected submitSignup(): void {
    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      return;
    }

    const { fullName } = this.signupForm.getRawValue();
    this.feedbackMessage.set(
      `Signup interface ready for ${fullName}. You can now connect this screen to your account creation endpoint.`
    );
  }

  protected hasControlError(
    form: { get(path: string): AbstractControl | null },
    controlName: string,
    errorCode?: string
  ): boolean {
    const control = form.get(controlName);

    if (!control || !control.touched) {
      return false;
    }

    return errorCode ? control.hasError(errorCode) : control.invalid;
  }

  protected showPasswordMismatch(): boolean {
    const confirmPassword = this.signupForm.controls.confirmPassword;
    return Boolean(
      this.signupForm.hasError('passwordMismatch') &&
        (confirmPassword.touched || this.signupForm.controls.password.touched)
    );
  }
}
