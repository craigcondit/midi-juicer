package org.randomcoder.midi.samples.mpx550;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.SysexMessage;

public enum Mpx550Parameter {

  SYSTEM_OUTPUT_LVL(21, 2, 0, 0x00),
  SYSTEM_INPUT_SRC(3, 2, 0, 0x01),
  SYSTEM_CLOCK_SRC(2, 2, 0, 0x02),
  SYSTEM_DIGITL_OUT(1, 2, 0, 0x03),
  SYSTEM_MIX_MODE(1, 2, 0, 0x04),
  SYSTEM_BYPASS_MODE(2, 2, 0, 0x05),
  SYSTEM_PRG_CHG_MODE(1, 2, 0, 0x06),
  SYSTEM_TEMPO_MODE(1, 2, 0, 0x07),
  SYSTEM_MIDI_PATCHS(1, 2, 0, 0x08),
  SYSTEM_MIDI_CHANNL(17, 2, 0, 0x09),
  SYSTEM_MIDI_PGM_CHG(2, 2, 0, 0x0a),
  SYSTEM_MIDI_CLOCK(1, 2, 0, 0x0b),
  SYSTEM_MIDI_OUT_THR(1, 2, 0, 0x0c),
  SYSTEM_MEM_PROTECT(1, 2, 0, 0x0d),
  SYSTEM_CMPRSR_MODE(1, 2, 0, 0x0e),
  SYSTEM_OPRTNG_MODE(2, 2, 0, 0x0f),
  SYSTEM_TEMPO_BPM(400, 2, 0, 0x10),
  SYSTEM_ALGORITHM(10, 2, 0, 0x11),
  SYSTEM_PRESET(256, 2, 0, 0x13),
  SYSTEM_LVL_BAL(127, 2, 0, 0x14),
  SYSTEM_CMP_RATIO(5, 2, 0, 0x18),
  SYSTEM_THRESHLD(32, 2, 0, 0x19),
  SYSTEM_CMP_ATTK(5, 2, 0, 0x1a),
  SYSTEM_CMP_RELS(6, 2, 0, 0x1b),
  SYSTEM_LCD_CNTRST(1, 2, 0, 0x1c),
  SYSTEM_AUTO_LOAD(1, 2, 2, 0x1c),
  SYS_EVENTS_DMP_USR_PRGS(3, 2, 1, 0x00),
  SYS_EVENTS_DUMP_CURRNT(0, 2, 1, 0x01),
  SYS_EVENTS_DUMP_SYS_ALL(0, 2, 1, 0x02),
  SYS_EVENTS_CLR_USR_PRGS(0, 2, 1, 0x03),
  SYS_EVENTS_FACTRY_INIT(0, 2, 1, 0x04),
  SYS_EVENTS_STORE_PGM(63, 2, 1, 0x05),
  VPANEL_EDIT_PAGE(4, 2, 2, 0x14),
  VPANEL_ADJUST(127, 2, 2, 0x17),
  VPANEL_LVL_BAL(100, 2, 2, 0x18),
  VPANEL_MIX(100, 2, 2, 0x19),
  PLATE_BASS_MULT(9, 3, 4, 0, 0x00),
  PLATE_DECAY(63, 3, 4, 0, 0x01),
  PLATE_BASS_XVR(60, 3, 4, 0, 0x02),
  PLATE_RT_HC(60, 3, 4, 0, 0x03),
  PLATE_PRE_DELAY(599, 3, 4, 0, 0x04),
  PLATE_REF_LVL_L(25, 3, 4, 0, 0x05),
  PLATE_REF_LVL_R(25, 3, 4, 0, 0x06),
  PLATE_REF_DELAY_L(599, 3, 4, 0, 0x07),
  PLATE_REF_DELAY_R(599, 3, 4, 0, 0x08),
  PLATE_EKO_DLY_L(599, 3, 4, 0, 0x09),
  PLATE_EKO_DLY_R(599, 3, 4, 0, 0x0a),
  PLATE_EKO_FBK_L(30, 3, 4, 0, 0x0b),
  PLATE_EKO_FBK_R(30, 3, 4, 0, 0x0c),
  PLATE_SIZE(144, 3, 4, 0, 0x0d),
  PLATE_DIFFUSION(100, 3, 4, 0, 0x0e),
  PLATE_SPIN(50, 3, 4, 0, 0x0f),
  PLATE_WANDER(255, 3, 4, 0, 0x10),
  PLATE_ATTACK(100, 3, 4, 0, 0x11),
  PLATE_SPREAD(255, 3, 4, 0, 0x12),
  PLATE_HF_RLLOFF(60, 3, 4, 0, 0x13),
  PLATE_RVB_LEVEL(25, 3, 4, 0, 0x14),
  CHAMBER_BASS_MULT(9, 3, 4, 1, 0x00),
  CHAMBER_DECAY(63, 3, 4, 1, 0x01),
  CHAMBER_BASS_XVR(60, 3, 4, 1, 0x02),
  CHAMBER_RT_HC(60, 3, 4, 1, 0x03),
  CHAMBER_PRE_DELAY(599, 3, 4, 1, 0x04),
  CHAMBER_REF_LVL_L(25, 3, 4, 1, 0x05),
  CHAMBER_REF_LVL_R(25, 3, 4, 1, 0x06),
  CHAMBER_REF_DELAY_L(599, 3, 4, 1, 0x07),
  CHAMBER_REF_DELAY_R(599, 3, 4, 1, 0x08),
  CHAMBER_EKO_DLY_L(599, 3, 4, 1, 0x09),
  CHAMBER_EKO_DLY_R(599, 3, 4, 1, 0x0a),
  CHAMBER_EKO_FBK_L(30, 3, 4, 1, 0x0b),
  CHAMBER_EKO_FBK_R(30, 3, 4, 1, 0x0c),
  CHAMBER_SIZE(144, 3, 4, 1, 0x0d),
  CHAMBER_DIFFUSION(100, 3, 4, 1, 0x0e),
  CHAMBER_SPIN(50, 3, 4, 1, 0x0f),
  CHAMBER_WANDER(255, 3, 4, 1, 0x10),
  CHAMBER_ATTACK(255, 3, 4, 1, 0x11),
  CHAMBER_SPREAD(255, 3, 4, 1, 0x12),
  CHAMBER_HF_RLLOFF(60, 3, 4, 1, 0x13),
  CHAMBER_RVB_LEVEL(25, 3, 4, 1, 0x14),
  INVERSE_DURATION(112, 3, 4, 2, 0x00),
  INVERSE_LOW_SLOPE(31, 3, 4, 2, 0x01),
  INVERSE_HIGH_SLOPE(31, 3, 4, 2, 0x02),
  INVERSE_BASS_XVR(60, 3, 4, 2, 0x03),
  INVERSE_RT_HC(60, 3, 4, 2, 0x04),
  INVERSE_PRE_DELAY(599, 3, 4, 2, 0x05),
  INVERSE_REF_LVL_L(25, 3, 4, 2, 0x06),
  INVERSE_REF_DELAY_L(500, 3, 4, 2, 0x07),
  INVERSE_REF_LVL_R(25, 3, 4, 2, 0x08),
  INVERSE_REF_DELAY_R(500, 3, 4, 2, 0x09),
  INVERSE_DIFFUSION(100, 3, 4, 2, 0x0a),
  INVERSE_SHAPE(255, 3, 4, 2, 0x0b),
  INVERSE_SPREAD(255, 3, 4, 2, 0x0c),
  INVERSE_HF_RLLOFF(60, 3, 4, 2, 0x0d),
  INVERSE_RVB_LEVEL(25, 3, 4, 2, 0x0e),
  AMBIENCE_DECAY(55, 3, 4, 3, 0x00),
  AMBIENCE_RT_HC(60, 3, 4, 3, 0x01),
  AMBIENCE_PRE_DELAY(50, 3, 4, 3, 0x02),
  AMBIENCE_SIZE(144, 3, 4, 3, 0x03),
  AMBIENCE_DIFFUSION(100, 3, 4, 3, 0x04),
  AMBIENCE_SPIN(50, 3, 4, 3, 0x05),
  AMBIENCE_WANDER(255, 3, 4, 3, 0x06),
  AMBIENCE_RVB_LBL(25, 3, 4, 3, 0x07),
  AMBIENCE_HF_RLLOFF(60, 3, 4, 3, 0x08),
  AMBIENCE_RVB_LEVEL(25, 3, 4, 3, 0x09),
  DX1_CASCADE_FBK(200, 4, 4, 4, 0, 3),
  DX1_HF_RLLOFF_1(60, 4, 4, 4, 0, 4),
  DX1_HF_RLLOFF_2(60, 4, 4, 4, 0, 5),
  DX1_DELAY_DLY_LVL(16, 4, 4, 4, 1, 0x00),
  DX1_DELAY_DELAY(200, 4, 4, 4, 1, 0x01),
  DX1_DELAY_DLY_FBK(100, 4, 4, 4, 1, 0x02),
  DX1_DELAY_DLY_XFBK(100, 4, 4, 4, 1, 0x03),
  DX1_DELAY_DLY_HI_CUT(60, 4, 4, 4, 1, 0x05),
  DX1_DELAY_DLY_LVL_1(25, 4, 4, 4, 1, 0x0a),
  DX1_DELAY_DLY_LVL_2(25, 4, 4, 4, 1, 0x0b),
  DX1_DELAY_DLY_LVL_3(25, 4, 4, 4, 1, 0x0c),
  DX1_DELAY_R_LVL_1(25, 4, 4, 4, 1, 0x0d),
  DX1_DELAY_R_LVL_2(25, 4, 4, 4, 1, 0x0e),
  DX1_DELAY_R_LVL_3(25, 4, 4, 4, 1, 0x0f),
  DX1_DELAY_L_DLY_1(2690, 4, 4, 4, 1, 0x10),
  DX1_DELAY_L_DLY_2(2690, 4, 4, 4, 1, 0x11),
  DX1_DELAY_L_DLY_3(2690, 4, 4, 4, 1, 0x12),
  DX1_DELAY_R_DLY_1(2690, 4, 4, 4, 1, 0x13),
  DX1_DELAY_R_DLY_2(2690, 4, 4, 4, 1, 0x14),
  DX1_DELAY_R_DLY_3(2690, 4, 4, 4, 1, 0x15),
  DX1_DELAY_L_L_FBK(200, 4, 4, 4, 1, 0x16),
  DX1_DELAY_L_R_FBK(200, 4, 4, 4, 1, 0x17),
  DX1_DELAY_R_R_FBK(200, 4, 4, 4, 1, 0x18),
  DX1_DELAY_R_L_FBK(200, 4, 4, 4, 1, 0x19),
  DX1_DELAY_L_L_LVL(100, 4, 4, 4, 1, 0x1a),
  DX1_DELAY_L_R_LVL(100, 4, 4, 4, 1, 0x1b),
  DX1_DELAY_R_R_LVL(100, 4, 4, 4, 1, 0x1c),
  DX1_DELAY_R_L_LVL(100, 4, 4, 4, 1, 0x1d),
  DX1_FX_CHORUS_SPEED_1(5000, 5, 4, 4, 2, 0, 0x00),
  DX1_FX_CHORUS_SPEED_2(5000, 5, 4, 4, 2, 0, 0x01),
  DX1_FX_CHORUS_SPREAD(100, 5, 4, 4, 2, 0, 0x02),
  DX1_FX_CHORUS_SWEEP_1(100, 5, 4, 4, 2, 0, 0x04),
  DX1_FX_CHORUS_SWEEP_2(100, 5, 4, 4, 2, 0, 0x05),
  DX1_FX_CHORUS_RES_1(200, 5, 4, 4, 2, 0, 0x06),
  DX1_FX_CHORUS_RES_2(200, 5, 4, 4, 2, 0, 0x07),
  DX1_FX_CHORUS_HF_RLLOFF(60, 5, 4, 4, 2, 0, 0x08),
  DX1_FX_CHORUS_DIFFUSION(100, 5, 4, 4, 2, 0, 0x09),
  DX1_FX_FLANGE_SPEED(5000, 5, 4, 4, 2, 1, 0x00),
  DX1_FX_FLANGE_SWEEP(100, 5, 4, 4, 2, 1, 0x02),
  DX1_FX_FLANGE_RESONANC(200, 5, 4, 4, 2, 1, 0x03),
  DX1_FX_FLANGE_PHASE(3, 5, 4, 4, 2, 1, 0x05),
  DX1_FX_FLANGE_DEPTH(200, 5, 4, 4, 2, 1, 0x06),
  DX1_FX_DETUNE_TUNE_1(100, 5, 4, 4, 2, 2, 0x00),
  DX1_FX_DETUNE_TUNE_2(100, 5, 4, 4, 2, 2, 0x01),
  DX1_FX_DETUNE_PRE_DELAY(25, 5, 4, 4, 2, 2, 0x02),
  DX2_CASCADE_FBK(200, 4, 4, 5, 0, 3),
  DX2_HF_RLLOFF_1(60, 4, 4, 5, 0, 4),
  DX2_HF_RLLOFF_2(60, 4, 4, 5, 0, 5),
  DX2_DELAY_DLY_LVL(16, 4, 4, 5, 1, 0x00),
  DX2_DELAY_DELAY(200, 4, 4, 5, 1, 0x01),
  DX2_DELAY_DLY_FBK(100, 4, 4, 5, 1, 0x02),
  DX2_DELAY_DLY_XFBK(100, 4, 4, 5, 1, 0x03),
  DX2_DELAY_DLY_HI_CUT(60, 4, 4, 5, 1, 0x05),
  DX2_DELAY_DLY_LVL_1(25, 4, 4, 5, 1, 0x0a),
  DX2_DELAY_DLY_LVL_2(25, 4, 4, 5, 1, 0x0b),
  DX2_DELAY_DLY_LVL_3(25, 4, 4, 5, 1, 0x0c),
  DX2_DELAY_R_LVL_1(25, 4, 4, 5, 1, 0x0d),
  DX2_DELAY_R_LVL_2(25, 4, 4, 5, 1, 0x0e),
  DX2_DELAY_R_LVL_3(25, 4, 4, 5, 1, 0x0f),
  DX2_DELAY_L_DLY_1(2760, 4, 4, 5, 1, 0x10),
  DX2_DELAY_L_DLY_2(2760, 4, 4, 5, 1, 0x11),
  DX2_DELAY_L_DLY_3(2760, 4, 4, 5, 1, 0x12),
  DX2_DELAY_R_DLY_1(2760, 4, 4, 5, 1, 0x13),
  DX2_DELAY_R_DLY_2(2760, 4, 4, 5, 1, 0x14),
  DX2_DELAY_R_DLY_3(2760, 4, 4, 5, 1, 0x15),
  DX2_DELAY_L_L_FBK(200, 4, 4, 5, 1, 0x16),
  DX2_DELAY_L_R_FBK(200, 4, 4, 5, 1, 0x17),
  DX2_DELAY_R_R_FBK(200, 4, 4, 5, 1, 0x18),
  DX2_DELAY_R_L_FBK(200, 4, 4, 5, 1, 0x19),
  DX2_DELAY_L_L_LVL(100, 4, 4, 5, 1, 0x1a),
  DX2_DELAY_L_R_LVL(100, 4, 4, 5, 1, 0x1b),
  DX2_DELAY_R_R_LVL(100, 4, 4, 5, 1, 0x1c),
  DX2_DELAY_R_L_LVL(100, 4, 4, 5, 1, 0x1d),
  DX2_FX_PITCH_FBK(200, 5, 4, 5, 2, 0, 0x01),
  DX2_FX_PITCH_PDLY_L(50, 5, 4, 5, 2, 0, 0x02),
  DX2_FX_PITCH_PCH_L(5000, 5, 4, 5, 2, 0, 0x03),
  DX2_FX_PITCH_INTRVL_1(36, 5, 4, 5, 2, 0, 0x04),
  DX2_FX_PITCH_PCH_FBK(200, 5, 4, 5, 2, 0, 0x05),
  DX2_FX_PITCH_PDLY_R(50, 5, 4, 5, 2, 0, 0x06),
  DX2_FX_PITCH_PCH_R_S(5000, 5, 4, 5, 2, 0, 0x07),
  DX2_FX_PITCH_INTRVL_2(36, 5, 4, 5, 2, 0, 0x08),
  DX2_FX_ROTARY_DRM_RATE(1000, 5, 4, 5, 2, 1, 0x00),
  DX2_FX_ROTARY_HRN_RATE(1000, 5, 4, 5, 2, 1, 0x01),
  DX2_FX_ROTARY_DELAY(0, 5, 4, 5, 2, 1, 0x02),
  DX2_FX_ROTARY_MSTR_RATE(100, 5, 4, 5, 2, 1, 0x03),
  DX2_FX_ROTARY_DRUM_DEP(100, 5, 4, 5, 2, 1, 0x04),
  DX2_FX_ROTARY_HORN_DEP(100, 5, 4, 5, 2, 1, 0x05),
  DX2_FX_ROTARY_DRUM_RES(200, 5, 4, 5, 2, 1, 0x06),
  DX2_FX_ROTARY_HORN_RES(200, 5, 4, 5, 2, 1, 0x07),
  DX2_FX_ROTARY_WIDTH(100, 5, 4, 5, 2, 1, 0x08),
  DX2_FX_ROTARY_BALANCE(200, 5, 4, 5, 2, 1, 0x09),
  DX2_FX_ROTARY_ACCLRTN_1(200, 5, 4, 5, 2, 1, 0x0a),
  DX2_FX_ROTARY_DCCLRTN_1(200, 5, 4, 5, 2, 1, 0x0b),
  DX2_FX_ROTARY_DRY_MIX(25, 5, 4, 5, 2, 1, 0x0c),
  DX2_FX_ROTARY_ACCLRTN_2(200, 5, 4, 5, 2, 1, 0x0d),
  DX2_FX_ROTARY_DCCLRTN_2(200, 5, 4, 5, 2, 1, 0x0e),
  DX2_FX_ROTARY_MSTR_DEPTH(100, 5, 4, 5, 2, 1, 0x0f),
  DX2_FX_TREMOLO_RATE(5000, 5, 4, 5, 2, 2, 0x00),
  DX2_FX_TREMOLO_INPUT_LVL(25, 5, 4, 5, 2, 2, 0x01),
  DX2_FX_TREMOLO_DEPTH(100, 5, 4, 5, 2, 2, 0x02),
  DX2_FX_TREMOLO_PHASE(3, 5, 4, 5, 2, 2, 0x03),
  DX2_FX_TREMOLO_WAVFORM(4, 5, 4, 5, 2, 2, 0x04);

  private final int maxValue;
  private final byte[] address;

  private Mpx550Parameter(int maxValue, int... data) {
    this.maxValue = maxValue;
    this.address = raw(data);
  }

  private static byte[] raw(int... data) {
    byte[] result = new byte[data.length * 4];
    for (int i = 0; i < data.length; i++) {
      result[i * 4] = (byte) (data[i] & 0xf);
      result[(i * 4) + 1] = (byte) ((data[i] >> 4) & 0xf);
      result[(i * 4) + 2] = (byte) ((data[i] >> 8) & 0xf);
      result[(i * 4) + 3] = (byte) ((data[i] >> 12) & 0xf);
    }
    return result;
  }

  SysexMessage buildQuery() {
    byte[] data = new byte[address.length + 8];
    data[0] = (byte) SysexMessage.SYSTEM_EXCLUSIVE;
    data[1] = 0x06; // Lexicon
    data[2] = 0x16; // MPX550
    data[3] = 0x00; // padding
    data[4] = 0x06; // message = query
    data[5] = 0x01; // query additional byte 1
    data[6] = 0x00; // query additional byte 2
    System.arraycopy(address, 0, data, 7, address.length);
    data[address.length + 7] = (byte) 0xf7; // end-of-message

    try {
      return new SysexMessage(data, data.length);
    } catch (InvalidMidiDataException e) {
      throw new RuntimeException("Invalid midi data", e);
    }
  }

  SysexMessage data(int value) {
    return new SysexMessage();
  }
}
