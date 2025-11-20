#include "principal.h"

lv_obj_t * scr_main = nullptr;

void go_main() {

    if(!scr_main) {

        scr_main = lv_obj_create(NULL);
        lv_scr_load(scr_main);
        lv_obj_set_size(scr_main, 320, 240);
        lv_obj_set_scroll_dir(scr_main, LV_DIR_NONE);
        lv_obj_set_style_bg_color(scr_main, lv_color_hex(0x1A3D6B), 0);
        lv_obj_set_style_bg_grad_color(scr_main, lv_color_hex(0xEA824D), 0);
        lv_obj_set_style_bg_grad_dir(scr_main, LV_GRAD_DIR_VER, 0);

        // CARD PRINCIPAL
        lv_obj_t * card = lv_obj_create(scr_main);
        lv_obj_set_size(card, lv_pct(96), lv_pct(95));
        lv_obj_center(card);
        lv_obj_set_style_radius(card, 16, 0);
        lv_obj_set_style_pad_all(card, 12, 0);
        lv_obj_set_style_pad_row(card, 10, 0);
        lv_obj_set_style_bg_opa(card, LV_OPA_40, 0);
        lv_obj_set_style_bg_color(card, lv_color_hex(0xFFFFFF), 0);
        lv_obj_set_flex_flow(card, LV_FLEX_FLOW_COLUMN);
        lv_obj_set_flex_align(card,
            LV_FLEX_ALIGN_START,
            LV_FLEX_ALIGN_CENTER,
            LV_FLEX_ALIGN_CENTER
        );

        // TÍTULO
        lv_obj_t * lbl_titulo = lv_label_create(card);
        lv_obj_set_style_text_font(lbl_titulo, &lv_font_montserrat_20, 0);
        lv_label_set_text(lbl_titulo, "Artigo: Costura de Manga");

        // SUBTÍTULO
        lv_obj_t * lbl_func = lv_label_create(card);
        lv_obj_set_style_text_font(lbl_func, &lv_font_montserrat_16, 0);
        lv_label_set_text(lbl_func, "Funcionario: Aline Rosa");

        // BARRA DE PROGRESSO
        lv_obj_t * barra = lv_bar_create(card);
        lv_obj_set_size(barra, lv_pct(100), 14);
        lv_bar_set_range(barra, 0, 150);
        lv_bar_set_value(barra, 126, LV_ANIM_OFF);

        lv_obj_set_style_bg_color(barra, lv_color_hex(0x2B74C8), LV_PART_INDICATOR);
        lv_obj_set_style_bg_grad_color(barra, lv_color_hex(0xEA824D), LV_PART_INDICATOR);
        lv_obj_set_style_bg_grad_dir(barra, LV_GRAD_DIR_HOR, LV_PART_INDICATOR);
        lv_obj_set_style_radius(barra, 999, 0);
        lv_obj_set_style_radius(barra, 999, LV_PART_INDICATOR);

        // Linha com valores
        lv_obj_t * linha = lv_obj_create(card);
        lv_obj_remove_style_all(linha);
        lv_obj_set_size(linha, lv_pct(100), LV_SIZE_CONTENT);

        lv_obj_set_flex_flow(linha, LV_FLEX_FLOW_ROW);
        lv_obj_set_flex_align(linha,
            LV_FLEX_ALIGN_SPACE_BETWEEN,
            LV_FLEX_ALIGN_CENTER,
            LV_FLEX_ALIGN_CENTER
        );

        lv_obj_t * lbl_valor = lv_label_create(linha);
        lv_label_set_text(lbl_valor, "126/150");

        lv_obj_t * lbl_percent = lv_label_create(linha);
        lv_label_set_text(lbl_percent, "84%");

        // BOTÃO PAUSAR
        lv_obj_t * btn_pausar = lv_button_create(card);
        lv_obj_set_width(btn_pausar, lv_pct(100));
        lv_obj_set_style_radius(btn_pausar, 10, 0);
        lv_obj_set_style_bg_opa(btn_pausar, LV_OPA_20, 0);
        lv_obj_set_style_pad_all(btn_pausar, 8, 0);         

        lv_obj_t * lbl_pausar = lv_label_create(btn_pausar);
        lv_label_set_text(lbl_pausar, LV_SYMBOL_PAUSE "  Pausar");
        lv_obj_center(lbl_pausar);
        lv_obj_add_event_cb(btn_pausar, [](lv_event_t * e) -> void {
        go_touch();
        }, LV_EVENT_CLICKED, NULL);    
       

        // LINHA DOS BOTÕES
        lv_obj_t * linha_btn = lv_obj_create(card);
        lv_obj_remove_style_all(linha_btn);
        lv_obj_set_size(linha_btn, lv_pct(100), LV_SIZE_CONTENT);
        lv_obj_set_flex_flow(linha_btn, LV_FLEX_FLOW_ROW);

        lv_obj_set_style_pad_column(linha_btn, 10, 0);
        lv_obj_set_style_pad_top(linha_btn, 2, 0);
        lv_obj_set_style_pad_bottom(linha_btn, 2, 0);

        lv_obj_set_flex_align(linha_btn,
            LV_FLEX_ALIGN_SPACE_BETWEEN,
            LV_FLEX_ALIGN_CENTER,
            LV_FLEX_ALIGN_CENTER
        );

        // BOTÃO FINALIZAR
        lv_obj_t * btn_final = lv_btn_create(linha_btn);
        lv_obj_set_width(btn_final, lv_pct(48));
        lv_obj_set_style_radius(btn_final, 10, 0);
        lv_obj_set_style_pad_all(btn_final, 8, 0);
        lv_obj_set_style_bg_color(btn_final, lv_palette_main(LV_PALETTE_GREEN), 0);

        lv_obj_t * lbl_final = lv_label_create(btn_final);
        lv_label_set_text(lbl_final, LV_SYMBOL_OK "  Finalizar");
        lv_obj_center(lbl_final);

        // BOTÃO CONFIG
        lv_obj_t * btn_cfg = lv_btn_create(linha_btn);
        lv_obj_set_width(btn_cfg, lv_pct(48));
        lv_obj_set_style_radius(btn_cfg, 10, 0);
        lv_obj_set_style_pad_all(btn_cfg, 8, 0);

        lv_obj_t * lbl_cfg = lv_label_create(btn_cfg);
        lv_label_set_text(lbl_cfg, LV_SYMBOL_SETTINGS "  Config.");
        lv_obj_center(lbl_cfg);
        lv_obj_add_event_cb(btn_cfg, [](lv_event_t * e) -> void {
        go_bl();
        }, LV_EVENT_CLICKED, NULL);    

        // RODAPÉ
        lv_obj_t * footer = lv_obj_create(card);
        lv_obj_remove_style_all(footer);
        lv_obj_set_size(footer, lv_pct(100), LV_SIZE_CONTENT);
        lv_obj_set_flex_flow(footer, LV_FLEX_FLOW_ROW);
        lv_obj_set_flex_align(footer,
            LV_FLEX_ALIGN_SPACE_BETWEEN,
            LV_FLEX_ALIGN_CENTER,
            LV_FLEX_ALIGN_CENTER
        );

        lv_obj_t * lbl_hora = lv_label_create(footer);
        lv_label_set_text(lbl_hora, "⏰ 14:35");

        lv_obj_t * lbl_timer = lv_label_create(footer);
        lv_label_set_text(lbl_timer, "⏱ 1:45:23");
    }
    lv_screen_load(scr_main); 
    lv_obj_add_flag(btn_exit, LV_OBJ_FLAG_HIDDEN);    // desabilita saída, já na tela principal
    
}

