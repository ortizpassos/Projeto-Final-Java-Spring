// Custom type declaration for sockjs-client to resolve TS7016 error
declare module 'sockjs-client' {
  interface SockJS {
    onopen: ((e?: any) => void) | null;
    onclose: ((e?: any) => void) | null;
    onmessage: ((e: { data: any }) => void) | null;
    send(data: string): void;
    close(): void;
    // Add any other methods or properties you use
  }
  const SockJS: {
    new (url: string, _reserved?: any, options?: any): SockJS;
  };
  export = SockJS;
}