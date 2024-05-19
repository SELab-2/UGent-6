
declare module "react" {
  interface InputHTMLAttributes<T> extends HTMLAttributes<T> {
      webkitdirectory?: string;
      directory?:string
      mozdirectory?: string
  }
}



declare global {
  interface Window {
   

  }
}

export {}