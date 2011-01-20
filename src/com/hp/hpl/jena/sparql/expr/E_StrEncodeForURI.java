/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import org.openjena.atlas.lib.Chars ;
import org.openjena.atlas.lib.StrUtils ;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype ;
import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.sse.Tags ;

public class E_StrEncodeForURI extends ExprFunction1
{
    private static final String symbol = Tags.tagStrEncodeForURI ;

    public E_StrEncodeForURI(Expr expr)
    {
        super(expr, symbol) ;
    }
    
    @Override
    public NodeValue eval(NodeValue v)
    { 
        Node n = v.asNode() ;
        if ( ! n.isLiteral() )
            throw new ExprEvalException("Not a literal") ;
        if ( n.getLiteralDatatype() != null )
        {
            if ( ! n.getLiteralDatatype().equals(XSDDatatype.XSDstring) )
                throw new ExprEvalException("Not a string literal") ;
        }
        
        
        String str = n.getLiteralLexicalForm() ;
        
        String encStr = StrUtils.encodeHex(str,'%', uri_all) ;
        // Convert to UTF-8.
        if ( containsNonASCII(encStr) )
        {
            byte[] b = StrUtils.asUTF8bytes(encStr) ;
            encStr = encodeNonASCII(b) ;
        }
        return NodeValue.makeString(encStr) ;
    }
    
    
    static String encodeNonASCII(byte[] bytes)
    {
        StringBuilder sw = new StringBuilder() ;
        for ( int i = 0 ; i < bytes.length ; i++ )
        {
            byte b = bytes[i] ;
            // Signed bytes ...
            if ( b > 0 )
            {
                sw.append((char)b) ;
                continue ;
            }
            
            int hi = (b & 0xF0) >> 4 ;
            int lo = b & 0xF ;
            sw.append('%') ;
            sw.append(Chars.hexDigitsUC[hi]) ;
            sw.append(Chars.hexDigitsUC[lo]) ;
        }
        return sw.toString() ;
    }

    static boolean containsNonASCII(String string)
    {
        boolean clean = true ;
        for ( int i = 0 ; i < string.length() ; i++ )
        {
            char ch = string.charAt(i) ;
            if ( ch >= 127 )
                return true;
        }
        return false ;
    }
    
    // Put somewhere
    static char uri_reserved[] = 
    {' ',
     '!', '*', '"', '\'', '(', ')', ';', ':', '@', '&', 
     '=', '+', '$', ',', '/', '?', '%', '#', '[', ']'} ;

    static char[] uri_other = {'<', '>', '~', '.', '{', '}', '|', '\\', '-', '`', '_', '^'} ;     
    
    static char[] uri_other2 = {'\n', '\r', '\t' } ;
    
    static char[] uri_all = new char[uri_reserved.length+uri_other.length+uri_other2.length] ;
    
    
//    static char[] uri_allx= {  ' ', '!', '*', '"', '\'', '(', ')', ';', ':', '@', '&', 
//                           '=', '+', '$', ',', '/', '?', '%', '#', '[', ']',
//                           '<', '>', '~', '.', '{', '}', '|', '\\', '-', '`', '_', '^',
//                           '\n', '\r', '\t'} ;

    static {
        int idx = 0 ;
        System.arraycopy(uri_reserved, 0, uri_all, idx, uri_reserved.length) ;
        idx += uri_reserved.length ;
        
        System.arraycopy(uri_other, 0, uri_all, idx, uri_other.length) ;
        idx += uri_other.length ;
        
        System.arraycopy(uri_other2, 0, uri_all, idx, uri_other2.length) ;
        idx += uri_other2.length ;
    }
    
        
        /* The encodeURI() function is used to encode a URI.
special except: , / ? : @ & = + $ #
special + , / ? : @ & = + $ #
 *
 *
 */
        
    
    @Override
    public Expr copy(Expr expr) { return new E_StrEncodeForURI(expr) ; } 
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010, 2011 Epimorphics Ltd.
 *  
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
