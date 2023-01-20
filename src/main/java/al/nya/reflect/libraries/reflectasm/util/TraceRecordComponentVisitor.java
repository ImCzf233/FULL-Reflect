// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package al.nya.reflect.libraries.reflectasm.util;

import al.nya.reflect.libraries.reflectasm.AnnotationVisitor;
import al.nya.reflect.libraries.reflectasm.Attribute;
import al.nya.reflect.libraries.reflectasm.Opcodes;
import al.nya.reflect.libraries.reflectasm.RecordComponentVisitor;
import al.nya.reflect.libraries.reflectasm.TypePath;

/**
 * A {@link RecordComponentVisitor} that prints the record components it visits with a {@link
 * Printer}.
 *
 * @author Remi Forax
 */
public final class TraceRecordComponentVisitor extends RecordComponentVisitor {

  /** The printer to convert the visited record component into text. */
  public final Printer printer;

  /**
   * Constructs a new {@link TraceRecordComponentVisitor}.
   *
   * @param printer the printer to convert the visited record component into text.
   */
  public TraceRecordComponentVisitor(final Printer printer) {
    this(null, printer);
  }

  /**
   * Constructs a new {@link TraceRecordComponentVisitor}.
   *
   * @param recordComponentVisitor the record component visitor to which to delegate calls. May be
   *     {@literal null}.
   * @param printer the printer to convert the visited record component into text.
   */
  public TraceRecordComponentVisitor(
      final RecordComponentVisitor recordComponentVisitor, final Printer printer) {
    super(/* latest api ='*/ Opcodes.ASM9, recordComponentVisitor);
    this.printer = printer;
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
    Printer annotationPrinter = printer.visitRecordComponentAnnotation(descriptor, visible);
    return new TraceAnnotationVisitor(
        super.visitAnnotation(descriptor, visible), annotationPrinter);
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(
      final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
    Printer annotationPrinter =
        printer.visitRecordComponentTypeAnnotation(typeRef, typePath, descriptor, visible);
    return new TraceAnnotationVisitor(
        super.visitTypeAnnotation(typeRef, typePath, descriptor, visible), annotationPrinter);
  }

  @Override
  public void visitAttribute(final Attribute attribute) {
    printer.visitRecordComponentAttribute(attribute);
    super.visitAttribute(attribute);
  }

  @Override
  public void visitEnd() {
    printer.visitRecordComponentEnd();
    super.visitEnd();
  }
}
