import 'bootstrap/dist/css/bootstrap.min.css';

// Polyfill para global (sockjs-client)
if (typeof (window as any).global === 'undefined') {
  (window as any).global = window;
}
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
