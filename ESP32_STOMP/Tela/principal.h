#ifndef PRINCIPAL_H
#define PRINCIPAL_H

#include <LVGL_CYD.h>

#ifdef __cplusplus
extern "C" {
#endif

extern lv_obj_t * scr_main;
extern lv_obj_t * btn_exit;  // <-- ADICIONE ESTA LINHA

// Função da tela principal
void go_main();

// Função definida no arquivo .ino (para poder ser chamada no .cpp)
void go_touch();
void go_bl();

#ifdef __cplusplus
}
#endif

#endif
